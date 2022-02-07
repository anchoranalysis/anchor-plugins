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
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelMapCreator;
import org.anchoranalysis.image.io.channel.input.ChannelMap;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.map.OpenedNamedChannels;
import org.anchoranalysis.image.io.channel.map.NamedChannelsMap;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

@RequiredArgsConstructor
class GroupingInput extends NamedChannelsInput {

    // START REQUIRED ARGUMENTS
    /** A virtual path uniquely representing this particular file. */
    private final Path virtualPath;

    /** The opened raster with multiple files. */
    private final OpenedImageFile openedFile;

    private final ChannelMapCreator channelMapCreator;
    // END REQUIRED ARGUMENTS

    private ChannelMap channelMap;

    private String inputName;

    @Override
    public int numberSeries() throws ImageIOException {
        return openedFile.numberSeries();
    }

    @Override
    public Dimensions dimensions(int stackIndexInSeries, Logger logger) throws ImageIOException {
        return openedFile.dimensionsForSeries(stackIndexInSeries, logger);
    }

    @Override
    public NamedChannelsMap createChannelsForSeries(int seriesIndex, Logger logger)
            throws ImageIOException {
        ensureChannelMapExists(logger);
        return new OpenedNamedChannels(openedFile, channelMap, seriesIndex);
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
    public int numberChannels(Logger logger) throws ImageIOException {
        ensureChannelMapExists(logger);
        return channelMap.names().size();
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        return openedFile.bitDepth(logger);
    }

    @Override
    public ImageMetadata metadata(int seriesIndex, Logger logger) throws ImageIOException {
        NamedChannelsMap channels = createChannelsForSeries(seriesIndex, logger);
        ImageTimestampsAttributes timestamps = openedFile.timestamps();
        return new ImageMetadata(
                channels.dimensions(logger),
                numberChannels(logger),
                numberFrames(),
                channels.isRGB(),
                bitDepth(logger),
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

    private void ensureChannelMapExists(Logger logger) throws ImageIOException {
        // Lazy creation
        if (channelMap == null) {
            try {
                channelMap = channelMapCreator.create(openedFile, logger);
            } catch (CreateException e) {
                throw new ImageIOException("Failed to create a channel-map", e);
            }
        }
    }
}
