/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
