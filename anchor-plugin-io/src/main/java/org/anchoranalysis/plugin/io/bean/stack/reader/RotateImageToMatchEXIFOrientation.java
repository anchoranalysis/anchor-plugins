package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.CalculateOrientationChange;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.bioformats.metadata.OrientationReader;

/**
 * Rotates an image to match any EXIF orientation information, if it exists.
 *
 * <p>The EXIF orientation is read separately from the underlying {@link StackReader}.
 *
 * <p>If the metadata cannot be successfully read, no rotation occurs, and currently no error
 * message is logged.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class RotateImageToMatchEXIFOrientation extends StackReaderOrientationCorrection {

    // START BEAN PROPERTIES
    /** Reads the image that is subsequently flattened. */
    @BeanField @Getter @Setter private StackReaderOrientationCorrection reader;
    // END BEAN PROPERTIES

    @Override
    public OpenedImageFile openFile(Path path) throws ImageIOException {
        return openFile(path, logger -> inferNeededOrientationChange(path, logger));
    }

    @Override
    public OpenedImageFile openFile(Path path, CalculateOrientationChange orientationCorrection)
            throws ImageIOException {
        return reader.openFile(path, orientationCorrection);
    }

    private static OrientationChange inferNeededOrientationChange(Path path, Logger logger)
            throws ImageIOException {
        try {
            if (ImageFileFormat.JPEG.matches(path) || ImageFileFormat.TIFF.matches(path)) {
                // If no orientation-correction data is available, we proceed, performing no
                // rotation.
                OrientationChange change =
                        OrientationReader.determineOrientationCorrection(path)
                                .orElse(OrientationChange.KEEP_UNCHANGED);

                if (change != OrientationChange.KEEP_UNCHANGED) {
                    logger.messageLogger()
                            .log("Reoriented image from EXIF tag: " + change.toString());
                }

                return change;
            } else {
                return OrientationChange.KEEP_UNCHANGED;
            }
        } catch (Exception e) {
            logger.errorReporter()
                    .recordError(
                            RotateImageToMatchEXIFOrientation.class,
                            "Avoiding any orientation change, as cannot determine orientation from EXIF metadata due to following error.",
                            e);
            return OrientationChange.KEEP_UNCHANGED;
        }
    }
}
