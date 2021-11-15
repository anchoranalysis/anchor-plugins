/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

/**
 * An {@link OpenedImageFile} whose dimensions will be altered from those specified in the
 * image-file.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class OpenedRasterAlterDimensions implements OpenedImageFile {

    @FunctionalInterface
    public static interface ConsiderUpdatedImageResolution {

        /**
         * A possibly-updated image resolution.
         *
         * @param resolution the existing image resolution.
         * @return a new image resolution or {@link Optional#empty} if no change should occur.
         * @throws ImageIOException if reading to / writing from the file-system fails.
         */
        Optional<Resolution> maybeUpdatedResolution(Optional<Resolution> resolution)
                throws ImageIOException;
    }

    private OpenedImageFile delegate;
    private ConsiderUpdatedImageResolution processor;

    @Override
    public int numberSeries() {
        return delegate.numberSeries();
    }

    @Override
    public TimeSequence open(int seriesIndex, Progress progress, Logger logger)
            throws ImageIOException {
        TimeSequence sequence = delegate.open(seriesIndex, progress, logger);

        for (Stack stack : sequence) {
            Optional<Resolution> resolution = processor.maybeUpdatedResolution(stack.resolution());
            resolution.ifPresent(stack::assignResolution);
        }
        return sequence;
    }

    @Override
    public Optional<List<String>> channelNames(Logger logger) throws ImageIOException {
        return delegate.channelNames(logger);
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        return delegate.numberChannels(logger);
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex, Logger logger) throws ImageIOException {

        Dimensions dimensions = delegate.dimensionsForSeries(seriesIndex, logger);

        Optional<Resolution> resolution = processor.maybeUpdatedResolution(dimensions.resolution());

        if (resolution.isPresent()) {
            return dimensions.duplicateChangeResolution(resolution);
        } else {
            return dimensions;
        }
    }

    @Override
    public int numberFrames(Logger logger) throws ImageIOException {
        return delegate.numberFrames(logger);
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        return delegate.isRGB();
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        return delegate.bitDepth(logger);
    }

    @Override
    public ImageTimestampsAttributes timestamps() throws ImageIOException {
        return delegate.timestamps();
    }

    @Override
    public void close() throws ImageIOException {
        delegate.close();
    }
}
