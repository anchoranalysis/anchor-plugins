package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.nio.file.Path;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;

/**
 * A base class for a {@link RasterWriter} delegates to another {@link RasterWriter} based on values of a {@link RasterWriteOptions}.  
 * 
 * @author Owen Feehan
 *
 */
public abstract class RasterWriterDelegateBase extends RasterWriter {

    @Override
    public String fileExtension(RasterWriteOptions writeOptions) {
        return selectDelegate(writeOptions).fileExtension(writeOptions);
    }

    @Override
    public void writeStack(
            Stack stack, Path filePath, boolean makeRGB, RasterWriteOptions writeOptions)
            throws RasterIOException {
        selectDelegate(writeOptions).writeStack(stack, filePath, makeRGB, writeOptions);
    }

    @Override
    public void writeStackSeries(
            StackSeries stackSeries,
            Path filePath,
            boolean makeRGB,
            RasterWriteOptions writeOptions)
            throws RasterIOException {
        selectDelegate(writeOptions).writeStackSeries(stackSeries, filePath, makeRGB, writeOptions);
    }

    /**
     * Selects a {@link RasterWriter} to use as a delegate.
     * 
     * @param writeOptions options that specify what kind of rasters will be written.
     * @return the selected {@link RasterWriter}
     */
    protected abstract RasterWriter selectDelegate(RasterWriteOptions writeOptions);
}
