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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.plugin.opencv.convert.ConvertFromMat;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * An implementation of {@link OpenedImageFile} for reading using OpenCV.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class OpenedRasterOpenCV implements OpenedImageFile {

    // START REQUIRED ARGUMENTS
    /** The path to open. */
    private final Path path;
    // END REQUIRED ARGUMENTS

    /** Lazily opened stack. */
    private Stack stack;

    @Override
    public TimeSequence open(int seriesIndex, Progress progress) throws ImageIOException {
        openStackIfNecessary();
        return new TimeSequence(stack);
    }

    @Override
    public int numberSeries() {
        return 1;
    }

    @Override
    public Optional<List<String>> channelNames() throws ImageIOException {
        openStackIfNecessary();
        return OptionalUtilities.createFromFlag(stack.isRGB(), RGBChannelNames.rgbList());
    }

    @Override
    public int numberChannels() throws ImageIOException {
        openStackIfNecessary();
        return stack.getNumberChannels();
    }

    @Override
    public int numberFrames() throws ImageIOException {
        return 1;
    }

    @Override
    public int bitDepth() throws ImageIOException {
        openStackIfNecessary();
        if (!stack.allChannelsHaveIdenticalType()) {
            throw new ImageIOException(
                    "Not all channels have identical channel type, so not calculating bit-depth.");
        }
        return stack.getChannel(0).getVoxelDataType().numberBits();
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        openStackIfNecessary();
        return stack.isRGB();
    }

    @Override
    public void close() throws ImageIOException {
        stack = null;
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex) throws ImageIOException {
        openStackIfNecessary();
        return stack.dimensions();
    }

    /** Opens the stack if has not already been opened. */
    private void openStackIfNecessary() throws ImageIOException {
        Mat image = Imgcodecs.imread(path.toString());

        try {
            stack = ConvertFromMat.toStack(image);
        } catch (OperationFailedException e) {
            throw new ImageIOException(e);
        }
    }
}
