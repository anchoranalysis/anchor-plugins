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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.map.ChannelMap;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.channel.NamedEntries;
import org.anchoranalysis.image.io.input.NamedChannelsInputPart;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeriesConcatenate;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.OpenedRaster;
import org.anchoranalysis.io.input.FileInput;

/**
 * Provides a set of channels as an input, each of which has a name.
 *
 * <p>The standard implementation of {@link NamedChannelsInputPart}
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class MapPart extends NamedChannelsInputPart {

    // START REQUIRED ARGUMENTS
    private final FileInput delegate;
    private final StackReader stackReader;
    private final ChannelMap channelMapCreator;

    /**
     * This is to correct for a problem with formats such as czi where the seriesIndex doesn't
     * indicate the total number of series but rather is incremented with each acquisition, so for
     * our purposes we treat it as if its 0
     */
    private final boolean useLastSeriesIndexOnly;
    // END REQUIRED ARGUMENTS

    // We cache a certain amount of stacks read for particular series
    private OpenedRaster openedRasterMemo = null;
    private NamedEntries channelMap = null;

    @Override
    public Dimensions dimensions(int seriesIndex) throws RasterIOException {
        return openedRaster().dimensionsForSeries(seriesIndex);
    }

    @Override
    public int numberSeries() throws RasterIOException {
        if (useLastSeriesIndexOnly) {
            return 1;
        } else {
            return openedRaster().numberSeries();
        }
    }

    @Override
    public boolean hasChannel(String channelName) throws RasterIOException {
        return channelMap().keySet().contains(channelName);
    }

    // Where most of our time is being taken up when opening a raster
    @Override
    public NamedChannelsForSeries createChannelsForSeries(
            int seriesIndex, ProgressReporter progressReporter) throws RasterIOException {

        // We always use the last one
        if (useLastSeriesIndexOnly) {
            seriesIndex = openedRaster().numberSeries() - 1;
        }

        NamedChannelsForSeriesConcatenate out = new NamedChannelsForSeriesConcatenate();
        out.add(new NamedChannelsForSeriesMap(openedRaster(), channelMap(), seriesIndex));
        return out;
    }

    @Override
    public String descriptiveName() {
        return delegate.descriptiveName();
    }

    @Override
    public List<Path> pathForBindingForAllChannels() {
        ArrayList<Path> out = new ArrayList<>();
        pathForBinding().ifPresent(out::add);
        return out;
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
        return openedRaster().numberChannels();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return openedRaster().bitDepth();
    }

    private NamedEntries channelMap() throws RasterIOException {
        openedRaster();
        return channelMap;
    }

    private OpenedRaster openedRaster() throws RasterIOException {
        if (openedRasterMemo == null) {
            openedRasterMemo =
                    stackReader.openFile(
                            delegate.pathForBinding()
                                    .orElseThrow(
                                            () ->
                                                    new RasterIOException(
                                                            "A binding-path is needed in the delegate.")));
            try {
                channelMap = channelMapCreator.createMap(openedRasterMemo);
            } catch (CreateException e) {
                throw new RasterIOException(e);
            }
        }
        return openedRasterMemo;
    }

    @Override
    public void close(ErrorReporter errorReporter) {

        if (openedRasterMemo != null) {
            try {
                openedRasterMemo.close();
            } catch (RasterIOException e) {
                errorReporter.recordError(MapPart.class, e);
            }
        }
        delegate.close(errorReporter);
    }
}
