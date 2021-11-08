package org.anchoranalysis.plugin.io.bean.metadata.header;

import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.spatial.box.Extent;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.png.PngDirectory;


/**
 * The headers found in a PNG file.
 * 
 * @author Owen Feehan
 *
 */
public class PNG extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.PNG;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(Metadata metadata)
            throws ImageIOException {
        
        Optional<Directory> directory = InferHelper.findDirectoryWithName(metadata, PngDirectory.class, "PNG-IHDR");
        if (!directory.isPresent()) {
            return Optional.empty();
        }
        
        Optional<Extent> extent = InferHelper.readFromWidthHeightTags(directory.get(), PngDirectory.TAG_IMAGE_WIDTH, PngDirectory.TAG_IMAGE_HEIGHT);
        if (!extent.isPresent()) {
            return Optional.empty();
        }
        
        Optional<Integer> numberChannels = numberOfChannels(directory.get());
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }
        
        Optional<Integer> bitDepth = InferHelper.readInt(directory.get(), PngDirectory.TAG_BITS_PER_SAMPLE);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }
        
        return Optional.of( createMetadata(extent.get(), numberChannels.get(), bitDepth.get()) );
    }
    
    /** Creates the {@link ImageMetadata} given the necessary ingredients. */
    private static ImageMetadata createMetadata(Extent extent, int numberChannels, int bitDepth) {
        // Image resolution is ignored.
        Dimensions dimensions = new Dimensions(extent);
        boolean rgb = numberChannels == 3 || numberChannels == 4;
        return new ImageMetadata(dimensions, numberChannels, 1, rgb, bitDepth);
    }
    
    private static Optional<Integer> numberOfChannels(Directory directory) throws ImageIOException {
        Optional<Integer> colorType = InferHelper.readInt(directory, PngDirectory.TAG_COLOR_TYPE);
        if (!colorType.isPresent()) {
            return Optional.empty();
        }
        switch (colorType.get()) {
            case 0:
                return Optional.of(1);  // Grayscale
            case 2:
            case 3:
                return Optional.of(3);  // Truecolor or indexed
            case 4:
                return Optional.of(2);  // Grayscale and alpha
            case 6:
                return Optional.of(4);  // Truecolor and alpha
            default:
                throw new ImageIOException("Unrecognised color-type in PNG header: " + colorType.get());
        }
    }
}
