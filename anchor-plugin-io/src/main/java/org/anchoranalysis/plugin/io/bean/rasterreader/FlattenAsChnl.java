/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

/**
 * Combines all series and frames returned by a reader by converting them into multiple channels in
 * the same image
 *
 * <p>It assumes that the underlying rasterReader will only return images with: 1. a constant number
 * of chnls 2. a constant number of frames
 *
 * @author Owen Feehan
 */
public class FlattenAsChnl extends RasterReader {

    // START BEAN PROPERTIES
    @BeanField private RasterReader rasterReader;
    // END BEAN PROPERTIES

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {

        OpenedRaster delegate = rasterReader.openFile(filepath);
        return new OpenedRasterImpl(delegate);
    }

    private static class OpenedRasterImpl extends OpenedRaster {

        private OpenedRaster delegate;
        private int numSeries;
        private int expectedNumChnl;
        private int expectedNumFrames;

        public OpenedRasterImpl(OpenedRaster delegate) throws RasterIOException {
            super();
            this.delegate = delegate;

            numSeries = delegate.numSeries();

            expectedNumChnl = delegate.numChnl();

            expectedNumFrames = delegate.numFrames();
        }

        @Override
        public int numSeries() {
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
        public int numChnl() {
            return expectedNumChnl * numSeries * expectedNumFrames;
        }

        @Override
        public int numFrames() {
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
        public ImageDimensions dim(int seriesIndex) throws RasterIOException {
            return delegate.dim(seriesIndex);
        }

        private List<Stack> extractStacksAndVerify(TimeSequence ts) throws RasterIOException {

            if (ts.size() != expectedNumFrames) {
                throw new RasterIOException(
                        String.format(
                                "This bean expects %d frames to always be returned from the rasterReader to only return images with a single time-frame, but it returned an image with %d frames",
                                expectedNumFrames, ts.size()));
            }

            List<Stack> out = new ArrayList<>();

            for (int i = 0; i < ts.size(); i++) {

                Stack stack = ts.get(i);

                if (stack.getNumChnl() != expectedNumChnl) {
                    throw new RasterIOException(
                            String.format(
                                    "This bean expects %d channels to always be returned from the rasterReader to only return images with a single time-frame, but it returned an image with %d channels",
                                    expectedNumChnl, stack.getNumChnl()));
                }

                out.add(stack);
            }

            return out;
        }

        /**
         * Adds all channels from src-stack to dest-stack
         *
         * @throws IncorrectImageSizeException
         */
        private static void addStack(List<Stack> src, Stack dest)
                throws IncorrectImageSizeException {
            for (Stack s : src) {
                addStack(s, dest);
            }
        }

        /**
         * Adds all channels from src-stack to dest-stack
         *
         * @throws IncorrectImageSizeException
         */
        private static void addStack(Stack src, Stack dest) throws IncorrectImageSizeException {
            for (Channel c : src) {
                dest.addChnl(c);
            }
        }
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }
}
