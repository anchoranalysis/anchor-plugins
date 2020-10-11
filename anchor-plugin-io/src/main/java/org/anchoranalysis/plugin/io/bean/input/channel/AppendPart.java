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

package org.anchoranalysis.plugin.io.bean.input.channel;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.input.NamedChannelsInputPart;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeriesConcatenate;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.OpenedRaster;
import org.anchoranalysis.io.exception.AnchorIOException;
import org.anchoranalysis.io.input.PathSupplier;

/**
 * Appends another channel to an existing NamedChannelInputBase
 *
 * @author Owen Feehan
 */
class AppendPart extends NamedChannelsInputPart {

    private NamedChannelsInputPart delegate;
    private AdditionalChannel additionalChannel;
    private StackReader stackReader;

    private OpenedRaster openedRasterMemo;

    public AppendPart(
            NamedChannelsInputPart delegate,
            String channelName,
            int channelIndex,
            PathSupplier filePath,
            StackReader stackReader) {
        super();
        this.delegate = delegate;
        this.additionalChannel = new AdditionalChannel(channelName, channelIndex, filePath);
        this.stackReader = stackReader;
    }

    @Override
    public int numberSeries() throws RasterIOException {
        return delegate.numberSeries();
    }

    @Override
    public Dimensions dimensions(int seriesIndex) throws RasterIOException {
        return delegate.dimensions(seriesIndex);
    }

    @Override
    public boolean hasChannel(String channelName) throws RasterIOException {

        if (additionalChannel.getName().equals(channelName)) {
            return true;
        }
        return delegate.hasChannel(channelName);
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(
            int seriesIndex, ProgressReporter progressReporter) throws RasterIOException {

        NamedChannelsForSeries exst = delegate.createChannelsForSeries(seriesIndex, progressReporter);

        openRasterIfNecessary();

        NamedChannelsForSeriesConcatenate out = new NamedChannelsForSeriesConcatenate();
        out.add(exst);
        out.add(
                new NamedChannelsForSeriesMap(
                        openedRasterMemo, additionalChannel.createChannelMap(), seriesIndex));
        return out;
    }

    private void openRasterIfNecessary() throws RasterIOException {
        try {
            Path filePathAdditional = additionalChannel.getFilePath();

            if (openedRasterMemo == null) {
                openedRasterMemo = stackReader.openFile(filePathAdditional);
            }

        } catch (AnchorIOException e) {
            throw new RasterIOException(e);
        }
    }

    @Override
    public String descriptiveName() {
        return delegate.descriptiveName();
    }

    @Override
    public List<Path> pathForBindingForAllChannels() throws OperationFailedException {
        try {
            List<Path> list = delegate.pathForBindingForAllChannels();
            list.add(additionalChannel.getFilePath());
            return list;

        } catch (AnchorIOException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public Optional<Path> pathForBinding() {
        return delegate.pathForBinding();
    }

    @Override
    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public int numberChannels() throws RasterIOException {
        return delegate.numberChannels();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return delegate.bitDepth();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        if (openedRasterMemo != null) {
            try {
                openedRasterMemo.close();
            } catch (RasterIOException e) {
                errorReporter.recordError(AppendPart.class, e);
            }
        }
        delegate.close(errorReporter);
    }
}
