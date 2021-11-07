package org.anchoranalysis.plugin.io.bean.metadata.reader;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.plugin.io.file.EXIFOrientationReader;

/**
 * Tries to construct the {@link ImageMetadata} from EXIF and other metadata, if available, or
 * otherwise falls back to another reader.
 *
 * <p>It supports a limited number of file-types, as identified by an extension in the path:
 *
 * <ul>
 *   <li>JPEG (.jpg or .jpeg)
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class FromEXIF extends ImageMetadataReader {

    // START BEAN PROPERTIES
    /** Fallback to use if EXIF information is non-existing or absent. */
    @BeanField @Getter @Setter private ImageMetadataReader reader;
    // END BEAN PROPERTIES

    @Override
    public ImageMetadata openFile(Path path, StackReader defaultStackReader)
            throws ImageIOException {

        if (ImageFileFormat.JPEG.matches(path)) {

            return OptionalUtilities.orElseGet(
                    attemptToPopulateFromMetadata(path),
                    () -> useFallbackReader(path, defaultStackReader));
        } else {
            return useFallbackReader(path, defaultStackReader);
        }
    }

    /**
     * Try to infer the needed elements for {@link ImageMetadata} from metadata present in {@code
     * path}.
     */
    private static Optional<ImageMetadata> attemptToPopulateFromMetadata(Path path)
            throws ImageIOException {
        try {
            Metadata metadata = com.drew.imaging.ImageMetadataReader.readMetadata(path.toFile());

            if (metadata == null) {
                return Optional.empty();
            }

            Optional<OrientationChange> orientation =
                    EXIFOrientationReader.determineOrientationCorrection(metadata);

            // Infer width and height from the metadata
            Optional<Dimensions> dimensions =
                    InferExtentFromMetadata.inferExtent(metadata, orientation).map(Dimensions::new);

            if (dimensions.isPresent()) {
                return inferRemainingAttributes(metadata, dimensions.get());
            }

            return Optional.empty();

        } catch (IOException | ImageProcessingException e) {
            throw new ImageIOException(e);
        }
    }

    /** Infers the remaining needed attributes, once the dimensions are known. */
    private static Optional<ImageMetadata> inferRemainingAttributes(
            Metadata metadata, Dimensions dimensions) throws ImageIOException {

        // Then infer the number of channels.
        Optional<Integer> numberChannels =
                InferAttributesFromMetadata.inferNumberChannels(metadata);
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }

        // Then infer the bit-depth
        Optional<Integer> bitDepth = InferAttributesFromMetadata.inferBitDepth(metadata);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        // Assume any image with three channels is RGB encoded.
        boolean rgb = numberChannels.get() == 3;
        return Optional.of(
                new ImageMetadata(
                        dimensions, numberChannels.get(), 1, rgb, bitDepth.get())); // NOSONAR
    }

    /** Use the fallback {@code ImageMetadataReader} to establish the metadata. */
    private ImageMetadata useFallbackReader(Path path, StackReader defaultStackReader)
            throws ImageIOException {
        return reader.openFile(path, defaultStackReader);
    }
}
