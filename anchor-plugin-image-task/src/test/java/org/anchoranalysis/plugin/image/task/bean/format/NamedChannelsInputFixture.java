/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.format;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.progress.ProgressIgnore;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.test.image.StackFixture;
import org.anchoranalysis.test.image.stackwriter.ChannelSpecification;

class NamedChannelsInputFixture extends NamedChannelsInput {

    private static final Extent EXTENT = new Extent(200, 20, 3);

    private final Stack stack;

    public NamedChannelsInputFixture(ChannelSpecification channelSpecification) {
        StackFixture fixture = new StackFixture();
        this.stack = fixture.create(channelSpecification, EXTENT);
    }

    @Override
    public String identifier() {
        return "someInput";
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.empty();
    }

    @Override
    public int numberSeries() throws ImageIOException {
        return 1;
    }

    @Override
    public Dimensions dimensions(int seriesIndex, Logger logger) throws ImageIOException {
        return stack.dimensions();
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        return stack.getNumberChannels();
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        return stack.getChannel(0).getVoxelDataType().bitDepth();
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(
            int seriesIndex, Progress progress, Logger logger) throws ImageIOException {
        return new NamedChannelsForSeriesFixture(stack);
    }

    @Override
    public ImageMetadata metadata(int seriesIndex, Logger logger) throws ImageIOException {
        NamedChannelsForSeries channels =
                createChannelsForSeries(seriesIndex, ProgressIgnore.get(), logger);
        ZonedDateTime now = ZonedDateTime.now();
        return new ImageMetadata(
                channels.dimensions(logger),
                numberChannels(logger),
                numberFrames(),
                channels.isRGB(),
                bitDepth(logger),
                new ImageFileAttributes(Paths.get("fakePath.png"), now, now),
                Optional.empty());
    }
}
