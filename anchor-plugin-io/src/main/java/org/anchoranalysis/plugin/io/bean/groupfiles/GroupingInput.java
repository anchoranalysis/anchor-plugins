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

package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.map.ChannelMap;
import org.anchoranalysis.image.io.channel.NamedEntries;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.OpenedRaster;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;

class GroupingInput extends NamedChannelsInput {

    // The opened raster with multiple files
    private OpenedRaster openedRaster = null;

    // A virtual path uniquely representing this particular file
    private Path virtualPath;

    private ChannelMap channelMapCreator;

    private NamedEntries channelMap = null;

    private String inputName;

    // The root object that is used to provide the inputName and pathForBinding
    //
    public GroupingInput(
            Path virtualPath, MultiFileReaderOpenedRaster mfor, ChannelMap channelMapCreator) {
        super();
        this.virtualPath = virtualPath;
        this.openedRaster = mfor;
        this.channelMapCreator = channelMapCreator;
    }

    @Override
    public int numberSeries() throws ImageIOException {
        return openedRaster.numberSeries();
    }

    @Override
    public Dimensions dimensions(int seriesIndex) throws ImageIOException {
        return openedRaster.dimensionsForSeries(seriesIndex);
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(
            int seriesIndex, ProgressReporter progressReporter) throws ImageIOException {
        ensureChannelMapExists();
        return new NamedChannelsForSeriesMap(openedRaster, channelMap, seriesIndex);
    }

    @Override
    public String name() {
        return inputName;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(virtualPath);
    }

    @Override
    public int numberChannels() throws ImageIOException {
        ensureChannelMapExists();
        return channelMap.keySet().size();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        try {
            openedRaster.close();
        } catch (ImageIOException e) {
            errorReporter.recordError(GroupingInput.class, e);
        }
    }

    private void ensureChannelMapExists() throws ImageIOException {
        // Lazy creation
        if (channelMap == null) {
            try {
                channelMap = channelMapCreator.createMap(openedRaster);
            } catch (CreateException e) {
                throw new ImageIOException(e);
            }
        }
    }

    @Override
    public int bitDepth() throws ImageIOException {
        return openedRaster.bitDepth();
    }
}
