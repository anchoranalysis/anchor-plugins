/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.CalculateOrientationChange;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.image.io.stack.time.TimeSeries;
import org.anchoranalysis.io.bioformats.metadata.ImageTimestampsAttributesFactory;
import org.anchoranalysis.plugin.opencv.convert.ConvertFromMat;
import org.opencv.core.Mat;

/**
 * An opened-image file using the OpenCV library.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class OpenedRasterOpenCV implements OpenedImageFile {

    // START REQUIRED ARGUMENTS
    /**
     * The path to open.
     *
     * <p>Note that only ascii paths are supported by OpenCV, <a
     * href="https://stackoverflow.com/questions/24769623/opencv-imread-on-windows-for-non-ascii-file-names">unicode
     * paths are not supported</a>.
     */
    private final Path path;

    /** Records the execution time of operations. */
    private final ExecutionTimeRecorder executionTimeRecorder;

    /** Calculates any change needed in orientation. */
    private final Optional<CalculateOrientationChange> calculateOrientation;

    /** A prefix used in the identifiers used for recording execution time. */
    private final String executionTimePrefix;

    /** How to read a {@link Mat} for a particular {@link Path}. */
    private final CheckedFunction<Path, Mat, IOException> readDecodeMat;
    // END REQUIRED ARGUMENTS

    /** Lazily opened stack. */
    private Stack stack;

    /** Lazily recorded timestamps. */
    private ImageTimestampsAttributes timestamps;

    @Override
    public TimeSeries open(int seriesIndex, Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        return new TimeSeries(stack);
    }

    @Override
    public int numberSeries() {
        return 1;
    }

    @Override
    public Optional<List<String>> channelNames(Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        boolean includeAlpha = numberChannels(logger) == 4;
        return OptionalFactory.create(stack.isRGB(), RGBChannelNames.asList(includeAlpha));
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        return stack.getNumberChannels();
    }

    @Override
    public int numberFrames(Logger logger) throws ImageIOException {
        return 1;
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        if (!stack.allChannelsHaveIdenticalType()) {
            throw new ImageIOException(
                    "Not all channels have identical channel type, so not calculating bit-depth.");
        }
        return stack.getChannel(0).getVoxelDataType().bitDepth();
    }

    @Override
    public boolean isRGB(Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        return stack.isRGB();
    }

    @Override
    public void close() throws ImageIOException {
        stack = null;
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex, Logger logger) throws ImageIOException {
        openStackIfNecessary(logger);
        return stack.dimensions();
    }

    @Override
    public ImageTimestampsAttributes timestamps() throws ImageIOException {
        if (timestamps == null) {
            timestamps = ImageTimestampsAttributesFactory.fromPath(path);
        }
        return timestamps;
    }

    /** Opens the stack if has not already been opened. */
    private void openStackIfNecessary(Logger logger) throws ImageIOException {
        if (stack == null) {

            try {
                Mat image =
                        executionTimeRecorder.recordExecutionTime(
                                executionTimePrefix + "reading/decoding the image.",
                                () -> readDecodeMat.apply(path));

                OrientationChanger.changeOrientationIfNecessary(
                        image, calculateOrientation, logger);

                stack =
                        executionTimeRecorder.recordExecutionTime(
                                executionTimePrefix + "convert OpenCV to stack",
                                () -> ConvertFromMat.toStack(image));
            } catch (OperationFailedException | IOException e) {
                throw new ImageIOException(
                        "Failed to convert an OpenCV image structure to a stack", e);
            }
        }
    }
}
