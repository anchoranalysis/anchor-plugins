/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

class OpenedRasterAlterDimensions extends OpenedRaster {

    private OpenedRaster delegate;
    private ConsiderUpdatedImageRes processor;

    @FunctionalInterface
    public static interface ConsiderUpdatedImageRes {

        /**
         * A possibly-updated image resolution
         *
         * @param res the existing image resolution
         * @return a new image resolution or empty if no change should occur
         * @throws RasterIOException
         */
        Optional<ImageResolution> maybeUpdatedResolution(ImageResolution res)
                throws RasterIOException;
    }

    public OpenedRasterAlterDimensions(OpenedRaster delegate, ConsiderUpdatedImageRes processor) {
        super();
        this.delegate = delegate;
        this.processor = processor;
    }

    @Override
    public int numSeries() {
        return delegate.numSeries();
    }

    @Override
    public TimeSequence open(int seriesIndex, ProgressReporter progressReporter)
            throws RasterIOException {
        TimeSequence ts = delegate.open(seriesIndex, progressReporter);

        for (Stack stack : ts) {
            Optional<ImageResolution> res =
                    processor.maybeUpdatedResolution(stack.getDimensions().getRes());
            res.ifPresent(stack::updateResolution);
        }
        return ts;
    }

    @Override
    public Optional<List<String>> channelNames() {
        return delegate.channelNames();
    }

    @Override
    public int numChnl() throws RasterIOException {
        return delegate.numChnl();
    }

    @Override
    public ImageDimensions dim(int seriesIndex) throws RasterIOException {

        ImageDimensions sd = delegate.dim(seriesIndex);

        Optional<ImageResolution> res = processor.maybeUpdatedResolution(sd.getRes());

        if (res.isPresent()) {
            return sd.duplicateChangeRes(res.get());
        } else {
            return sd;
        }
    }

    @Override
    public int numFrames() throws RasterIOException {
        return delegate.numFrames();
    }

    @Override
    public boolean isRGB() throws RasterIOException {
        return delegate.isRGB();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return delegate.bitDepth();
    }

    @Override
    public void close() throws RasterIOException {
        delegate.close();
    }
}
