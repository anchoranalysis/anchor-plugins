package org.anchoranalysis.plugin.opencv.bean.stack;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Reads image files with OpenCV accepting only ASCII-encoded paths.
 *
 * <p>It achieves this by uses the {@link Imgcodecs#imread}, and this approach is significantly
 * faster than {@code OpenCVReaderNonAscii}.
 *
 * <p>Unlike {@code OpenCVReaderNonAscii}, EXIF orientation correct occurs automatically, and we
 * must not address it explicitly.
 *
 * @author Owen Feehan
 */
class OpenCVReaderAscii extends StackReader {

    @Override
    public OpenedImageFile openFile(Path path, ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {
        return new OpenedRasterOpenCV(
                path,
                executionTimeRecorder,
                Optional.empty(),
                "OpenCV ASCII: ",
                pathToRead -> Imgcodecs.imread(pathToRead.toString()));
    }
}
