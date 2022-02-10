package org.anchoranalysis.plugin.opencv.bean.stack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.CalculateOrientationChange;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Reads image files with OpenCV tolerating non-ascii paths.
 *
 * <p>It achieves this by uses the {@link Imgcodecs#imdecode}, but this approach is significantly
 * slower than {@code OpenCVReaderAscii} (observed as approximately 4 times slower empirically).
 *
 * <p>Unlike {@code OpenCVReaderAscii}, No automatic EXIF orientation occurs, so it becomes
 * necessary to check and correct for this explicitly.
 *
 * @author Owen Feehan
 */
class OpenCVReaderNonAscii extends StackReaderOrientationCorrection {

    /** Prefix used in execution operation time identifiers. */
    private static final String EXECUTION_TIME_PREFIX = "OpenCV Non-ASCII: ";

    @Override
    public OpenedImageFile openFile(Path path, ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {
        return new OpenedRasterOpenCV(
                path,
                executionTimeRecorder,
                Optional.empty(),
                EXECUTION_TIME_PREFIX,
                OpenCVReaderNonAscii::readDecodeMat);
    }

    @Override
    public OpenedImageFile openFile(
            Path path,
            CalculateOrientationChange orientationCorrection,
            ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {
        return new OpenedRasterOpenCV(
                path,
                executionTimeRecorder,
                Optional.of(orientationCorrection),
                EXECUTION_TIME_PREFIX,
                OpenCVReaderNonAscii::readDecodeMat);
    }

    /**
     * Reads an image as bytes from the file-system, and then separately decodes those bytes into a
     * {@link Mat}.
     */
    private static Mat readDecodeMat(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        Mat mat = new MatOfByte(bytes);
        return Imgcodecs.imdecode(mat, Imgcodecs.IMREAD_UNCHANGED);
    }
}
