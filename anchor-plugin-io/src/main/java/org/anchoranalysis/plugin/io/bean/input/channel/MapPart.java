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
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelMap;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInputPart;
import org.anchoranalysis.image.io.channel.input.NamedEntries;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.input.file.FileInput;

/**
 * Provides a set of channels as an input, each of which has a name.
 *
 * <p>This is the standard implementation of {@link NamedChannelsInputPart}.
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

    private final ExecutionTimeRecorder executionTimeRecorder;
    // END REQUIRED ARGUMENTS

    // We cache a certain amount of stacks read for particular series
    private OpenedImageFile openedFileMemo = null;
    private NamedEntries channelMap = null;

    @Override
    public Dimensions dimensions(int stackIndexInSeries, Logger logger) throws ImageIOException {
        return openedFile().dimensionsForSeries(stackIndexInSeries, logger);
    }

    @Override
    public int numberSeries() throws ImageIOException {
        if (useLastSeriesIndexOnly) {
            return 1;
        } else {
            return openedFile().numberSeries();
        }
    }

    @Override
    public boolean hasChannel(String channelName, Logger logger) throws ImageIOException {
        return channelMap(logger).keySet().contains(channelName);
    }

    // Where most of our time is being taken up when opening a raster
    @Override
    public NamedChannelsForSeries createChannelsForSeries(
            int seriesIndex, Progress progress, Logger logger) throws ImageIOException {

        // We always use the last one
        if (useLastSeriesIndexOnly) {
            seriesIndex = openedFile().numberSeries() - 1;
        }

        return new NamedChannelsForSeriesMap(openedFile(), channelMap(logger), seriesIndex);
    }

    @Override
    public String identifier() {
        return delegate.identifier();
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
    public int numberChannels(Logger logger) throws ImageIOException {
        return openedFile().numberChannels(logger);
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        return openedFile().bitDepth(logger);
    }

    @Override
    public void close(ErrorReporter errorReporter) {

        if (openedFileMemo != null) {
            try {
                openedFileMemo.close();
            } catch (ImageIOException e) {
                errorReporter.recordError(MapPart.class, e);
            }
        }
        delegate.close(errorReporter);
    }

    @Override
    public ImageMetadata metadata(int seriesIndex, Logger logger) throws ImageIOException {
        return openedFile().metadata(seriesIndex, logger);
    }

    /** Create a channel-map, reusing the existing map, if it already exists. */
    private NamedEntries channelMap(Logger logger) throws ImageIOException {
        if (channelMap == null) {
            try {
                channelMap = channelMapCreator.createMap(openedFileMemo, logger);
            } catch (CreateException e) {
                throw new ImageIOException("Failed to create channel-map", e);
            }
        }
        return channelMap;
    }

    /**
     * Opens the file to create a {@link OpenedImageFile}, reusing the existing opened-file, if it
     * already exists.
     */
    private OpenedImageFile openedFile() throws ImageIOException {
        if (openedFileMemo == null) {
            Path path =
                    delegate.pathForBinding()
                            .orElseThrow(
                                    () ->
                                            new ImageIOException(
                                                    "A binding-path is needed in the delegate."));
            openedFileMemo = stackReader.openFile(path, executionTimeRecorder);
        }
        return openedFileMemo;
    }
}
