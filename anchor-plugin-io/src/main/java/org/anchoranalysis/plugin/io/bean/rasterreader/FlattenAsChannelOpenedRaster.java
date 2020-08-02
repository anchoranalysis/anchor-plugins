package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

class FlattenAsChannelOpenedRaster implements OpenedRaster {

    private OpenedRaster delegate;
    private int numSeries;
    private int expectedNumberChannels;
    private int expectedNumberFrames;

    public FlattenAsChannelOpenedRaster(OpenedRaster delegate) throws RasterIOException {
        super();
        this.delegate = delegate;

        numSeries = delegate.numberSeries();

        expectedNumberChannels = delegate.numberChannels();

        expectedNumberFrames = delegate.numberFrames();
    }

    @Override
    public int numberSeries() {
        // Always a single series
        return 1;
    }

    @Override
    public TimeSequence open(int seriesIndex, ProgressReporter progressReporter)
            throws RasterIOException {
        // We open each-series, verify assumptions, and combine the channels

        try {
            Stack out = new Stack();

            for (int i = 0; i < numSeries; i++) {

                TimeSequence ts = delegate.open(seriesIndex, progressReporter);

                addStack(extractStacksAndVerify(ts), out);
            }

            return new TimeSequence(out);

        } catch (IncorrectImageSizeException e) {
            throw new RasterIOException(e);
        }
    }

    @Override
    public Optional<List<String>> channelNames() {
        // We do not report channel-names, as we create them from the series
        return Optional.empty();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return delegate.bitDepth();
    }

    @Override
    public int numberChannels() {
        return expectedNumberChannels * numSeries * expectedNumberFrames;
    }

    @Override
    public int numberFrames() {
        // We make this assumption, and check each sequence we open
        return 1;
    }

    @Override
    public boolean isRGB() throws RasterIOException {
        return false;
    }

    @Override
    public void close() throws RasterIOException {
        delegate.close();
    }

    @Override
    public ImageDimensions dimensionsForSeries(int seriesIndex) throws RasterIOException {
        return delegate.dimensionsForSeries(seriesIndex);
    }

    private List<Stack> extractStacksAndVerify(TimeSequence ts) throws RasterIOException {

        if (ts.size() != expectedNumberFrames) {
            throw new RasterIOException(
                    String.format(
                            "This bean expects %d frames to always be returned from the rasterReader to only return images with a single time-frame, but it returned an image with %d frames",
                            expectedNumberFrames, ts.size()));
        }

        List<Stack> out = new ArrayList<>();

        for (int i = 0; i < ts.size(); i++) {

            Stack stack = ts.get(i);

            if (stack.getNumberChannels() != expectedNumberChannels) {
                throw new RasterIOException(
                        String.format(
                                "This bean expects %d channels to always be returned from the rasterReader to only return images with a single time-frame, but it returned an image with %d channels",
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
