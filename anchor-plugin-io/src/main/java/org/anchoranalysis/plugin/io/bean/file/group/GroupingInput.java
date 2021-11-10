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

package org.anchoranalysis.plugin.io.bean.file.group;

import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.progress.ProgressIgnore;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelMap;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.input.NamedEntries;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

@RequiredArgsConstructor
class GroupingInput extends NamedChannelsInput {

    // START REQUIRED ARGUMENTS
    /** A virtual path uniquely representing this particular file. */
    private final Path virtualPath;

    /** The opened raster with multiple files. */
    private final OpenedImageFile openedFile;

    private final ChannelMap channelMapCreator;
    // END REQUIRED ARGUMENTS

    private NamedEntries channelMap;

    private String inputName;

    @Override
    public int numberSeries() throws ImageIOException {
        return openedFile.numberSeries();
    }

    @Override
    public Dimensions dimensions(int stackIndexInSeries) throws ImageIOException {
        return openedFile.dimensionsForSeries(stackIndexInSeries);
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(int seriesIndex, Progress progress)
            throws ImageIOException {
        ensureChannelMapExists();
        return new NamedChannelsForSeriesMap(openedFile, channelMap, seriesIndex);
    }

    @Override
    public String identifier() {
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
    public int bitDepth() throws ImageIOException {
        return openedFile.bitDepth();
    }

    @Override
    public ImageMetadata metadata(int seriesIndex) throws ImageIOException {
        NamedChannelsForSeries channels =
                createChannelsForSeries(seriesIndex, ProgressIgnore.get());
        ImageTimestampsAttributes timestamps = openedFile.timestamps();
        return new ImageMetadata(
                channels.dimensions(),
                numberChannels(),
                numberFrames(),
                channels.isRGB(),
                bitDepth(),
                timestamps.getAttributes(),
                timestamps.getAcqusitionTime());
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        try {
            openedFile.close();
        } catch (ImageIOException e) {
            errorReporter.recordError(GroupingInput.class, e);
        }
    }

    private void ensureChannelMapExists() throws ImageIOException {
        // Lazy creation
        if (channelMap == null) {
            try {
                channelMap = channelMapCreator.createMap(openedFile);
            } catch (CreateException e) {
                throw new ImageIOException("Failed to create a channel-map", e);
            }
        }
    }
}
