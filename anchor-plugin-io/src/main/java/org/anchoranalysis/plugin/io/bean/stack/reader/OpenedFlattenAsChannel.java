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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

class OpenedFlattenAsChannel implements OpenedImageFile {

    private final OpenedImageFile delegate;
    private final int numberSeries;
    private final int expectedNumberChannels;
    private final int expectedNumberFrames;

    public OpenedFlattenAsChannel(OpenedImageFile delegate) throws ImageIOException {
        this.delegate = delegate;

        numberSeries = delegate.numberSeries();

        expectedNumberChannels = delegate.numberChannels();

        expectedNumberFrames = delegate.numberFrames();
    }

    @Override
    public int numberSeries() {
        // Always a single series
        return 1;
    }

    @Override
    public TimeSequence open(int seriesIndex, Progress progress) throws ImageIOException {
        // We open each-series, verify assumptions, and combine the channels

        try {
            Stack out = new Stack();

            for (int i = 0; i < numberSeries; i++) {

                TimeSequence ts = delegate.open(seriesIndex, progress);

                addStack(extractStacksAndVerify(ts), out);
            }

            return new TimeSequence(out);

        } catch (IncorrectImageSizeException e) {
            throw new ImageIOException(e);
        }
    }

    @Override
    public Optional<List<String>> channelNames() {
        // We do not report channel-names, as we create them from the series
        return Optional.empty();
    }

    @Override
    public int bitDepth() throws ImageIOException {
        return delegate.bitDepth();
    }

    @Override
    public int numberChannels() {
        return expectedNumberChannels * numberSeries * expectedNumberFrames;
    }

    @Override
    public int numberFrames() {
        // We make this assumption, and check each sequence we open
        return 1;
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        return false;
    }

    @Override
    public void close() throws ImageIOException {
        delegate.close();
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex) throws ImageIOException {
        return delegate.dimensionsForSeries(seriesIndex);
    }

    private List<Stack> extractStacksAndVerify(TimeSequence ts) throws ImageIOException {

        if (ts.size() != expectedNumberFrames) {
            throw new ImageIOException(
                    String.format(
                            "This bean expects %d frames to always be returned from the stackReader to only return images with a single time-frame, but it returned an image with %d frames",
                            expectedNumberFrames, ts.size()));
        }

        List<Stack> out = new ArrayList<>();

        for (int i = 0; i < ts.size(); i++) {

            Stack stack = ts.get(i);

            if (stack.getNumberChannels() != expectedNumberChannels) {
                throw new ImageIOException(
                        String.format(
                                "This bean expects %d channels to always be returned from the stackReader to only return images with a single time-frame, but it returned an image with %d channels",
                                expectedNumberChannels, stack.getNumberChannels()));
            }

            out.add(stack);
        }

        return out;
    }

    /**
     * Adds all channels from source-stacks to destination-stack
     *
     * @throws IncorrectImageSizeException
     */
    private static void addStack(List<Stack> source, Stack destination)
            throws IncorrectImageSizeException {
        for (Stack stack : source) {
            addStack(stack, destination);
        }
    }

    /**
     * Adds all channels from source-stack to destination-stack
     *
     * @throws IncorrectImageSizeException
     */
    private static void addStack(Stack source, Stack destination)
            throws IncorrectImageSizeException {
        for (Channel channel : source) {
            destination.addChannel(channel);
        }
    }
}