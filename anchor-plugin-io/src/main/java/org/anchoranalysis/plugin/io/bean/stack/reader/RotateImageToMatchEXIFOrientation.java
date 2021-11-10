package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.bioformats.metadata.OrientationReader;

/**
 * Rotates an image to match any EXIF orientation information, if it exists.
 *
 * <p>The EXIF orientation is read separately from the underlying {@link StackReader}.
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
        return openFile(path, inferNeededOrientationChange(path));
    }

    @Override
    public OpenedImageFile openFile(Path path, OrientationChange orientationCorrection)
            throws ImageIOException {
        return reader.openFile(path, orientationCorrection);
    }

    private static OrientationChange inferNeededOrientationChange(Path path)
            throws ImageIOException {
        // If no orientation-correction data is available, we proceed, performing no rotation.
        return OrientationReader.determineOrientationCorrection(path)
                .orElse(OrientationChange.KEEP_UNCHANGED);
    }
}
