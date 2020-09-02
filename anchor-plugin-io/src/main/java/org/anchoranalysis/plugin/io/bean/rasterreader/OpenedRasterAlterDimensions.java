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

package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

@AllArgsConstructor
class OpenedRasterAlterDimensions implements OpenedRaster {

    @FunctionalInterface
    public static interface ConsiderUpdatedImageResolution {

        /**
         * A possibly-updated image resolution
         *
         * @param resolution the existing image resolution
         * @return a new image resolution or empty if no change should occur
         * @throws RasterIOException
         */
        Optional<Resolution> maybeUpdatedResolution(Resolution resolution) throws RasterIOException;
    }

    private OpenedRaster delegate;
    private ConsiderUpdatedImageResolution processor;

    @Override
    public int numberSeries() {
        return delegate.numberSeries();
    }

    @Override
    public TimeSequence open(int seriesIndex, ProgressReporter progressReporter)
            throws RasterIOException {
        TimeSequence ts = delegate.open(seriesIndex, progressReporter);

        for (Stack stack : ts) {
            Optional<Resolution> res =
                    processor.maybeUpdatedResolution(stack.resolution());
            res.ifPresent(stack::updateResolution);
        }
        return ts;
    }

    @Override
    public Optional<List<String>> channelNames() {
        return delegate.channelNames();
    }

    @Override
    public int numberChannels() throws RasterIOException {
        return delegate.numberChannels();
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex) throws RasterIOException {

        Dimensions dimensions = delegate.dimensionsForSeries(seriesIndex);

        Optional<Resolution> res = processor.maybeUpdatedResolution(dimensions.resolution());

        if (res.isPresent()) {
            return dimensions.duplicateChangeRes(res.get());
        } else {
            return dimensions;
        }
    }

    @Override
    public int numberFrames() throws RasterIOException {
        return delegate.numberFrames();
    }

    @Override
    public boolean isRGB() throws RasterIOException {
        return delegate.isRGB();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return delegate.bitDepth();
    }

    @Override
    public void close() throws RasterIOException {
        delegate.close();
    }
}
