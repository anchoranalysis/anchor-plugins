package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderUnary;
import org.anchoranalysis.image.io.channel.input.OrientationCorrectionNeeded;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

/**
 * Rotates an image to match any EXIF orientation information, if it exists.
 *
 * <p>The EXIF orientation is read separately from the underlying {@link StackReader}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class RotateImageToMatchEXIFOrientation extends StackReaderUnary {

    /**
     * Creates with a {@link StackReader} to use as a delegate.
     *
     * @param stackReader the delegate stack-reader.
     */
    public RotateImageToMatchEXIFOrientation(StackReader stackReader) {
        super(stackReader);
    }

    @Override
    protected OpenedImageFile wrapOpenedFile(Path path, OpenedImageFile openedFileFromDelegate)
            throws ImageIOException {

        Optional<OrientationCorrectionNeeded> rotation =
                EXIFOrientationReader.rotationNeededClockwise(path);

        return openedFileFromDelegate;
    }
}
