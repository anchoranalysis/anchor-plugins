package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.png.PngDirectory;
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;
import org.anchoranalysis.spatial.box.Extent;

/**
 * The headers found in a PNG file.
 *
 * @author Owen Feehan
 */
public class PNG extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.PNG;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes timestamps) throws ImageIOException {

        Optional<Directory> directory =
                ReadMetadataUtilities.findDirectoryWithName(
                        metadata, PngDirectory.class, "PNG-IHDR");
        if (!directory.isPresent()) {
            return Optional.empty();
        }

        Optional<Extent> extent =
                ReadMetadataUtilities.readFromWidthHeightTags(
                        directory.get(),
                        PngDirectory.TAG_IMAGE_WIDTH,
                        PngDirectory.TAG_IMAGE_HEIGHT);
        if (!extent.isPresent()) {
            return Optional.empty();
        }

        Optional<Integer> numberChannels = numberOfChannels(directory.get());
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }

        Optional<Integer> bitDepth =
                ReadMetadataUtilities.readInt(directory.get(), PngDirectory.TAG_BITS_PER_SAMPLE);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(
                MetadataFactory.createMetadata(extent.get(), numberChannels.get(), bitDepth.get(), timestamps));
    }

    private static Optional<Integer> numberOfChannels(Directory directory) throws ImageIOException {
        Optional<Integer> colorType =
                ReadMetadataUtilities.readInt(directory, PngDirectory.TAG_COLOR_TYPE);
        if (!colorType.isPresent()) {
            return Optional.empty();
        }
        switch (colorType.get()) {
            case 0:
                return Optional.of(1); // Grayscale
            case 2:
            case 3:
                return Optional.of(3); // Truecolor or indexed
            case 4:
                return Optional.of(2); // Grayscale and alpha
            case 6:
                return Optional.of(4); // Truecolor and alpha
            default:
                throw new ImageIOException(
                        "Unrecognised color-type in PNG header: " + colorType.get());
        }
    }
}
