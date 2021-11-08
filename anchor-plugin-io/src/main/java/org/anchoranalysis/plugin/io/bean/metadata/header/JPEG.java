package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.io.file.EXIFOrientationReader;

/**
 * The headers found in a JPEG file.
 *
 * @author Owen Feehan
 */
public class JPEG extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.JPEG;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(Metadata metadata, ImageFileAttributes timestamps)
            throws ImageIOException {
        Optional<OrientationChange> orientation =
                EXIFOrientationReader.determineOrientationCorrection(metadata);

        // Infer width and height from the metadata.
        // Image resolution is ignored.
        Optional<Dimensions> dimensions =
                FromExifIfPossible.inferExtentFromEXIFOr(metadata, orientation)
                        .map(Dimensions::new);

        if (dimensions.isPresent()) {
            return inferRemainingAttributes(metadata, dimensions.get(), timestamps);
        }

        return Optional.empty();
    }

    /** Infers the remaining needed attributes, once the dimensions are known. */
    private static Optional<ImageMetadata> inferRemainingAttributes(
            Metadata metadata, Dimensions dimensions, ImageFileAttributes timestamps) {

        // Then infer the number of channels.
        Optional<Integer> numberChannels = inferNumberChannels(metadata);
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }

        // Then infer the bit-depth
        Optional<Integer> bitDepth = inferBitDepth(metadata);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        // Assume any image with three channels is RGB encoded.
        boolean rgb = numberChannels.get() == 3;
        return Optional.of(
                new ImageMetadata(
                        dimensions, numberChannels.get(), 1, rgb, bitDepth.get(), timestamps)); // NOSONAR
    }

    /**
     * Infers the <b>number of channels</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the number of channels.
     */
    private static Optional<Integer> inferNumberChannels(Metadata metadata) {
        return InferHelper.readInt(
                metadata, JpegDirectory.class, JpegDirectory.TAG_NUMBER_OF_COMPONENTS);
    }

    /**
     * Infers the <b>bit depth</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the bit depth.
     */
    private static Optional<Integer> inferBitDepth(Metadata metadata) {
        return InferHelper.readInt(metadata, JpegDirectory.class, JpegDirectory.TAG_DATA_PRECISION);
    }
}
