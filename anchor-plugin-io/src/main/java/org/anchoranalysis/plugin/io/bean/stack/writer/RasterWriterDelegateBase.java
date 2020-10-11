package org.anchoranalysis.plugin.io.bean.stack.writer;

import java.nio.file.Path;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.stack.StackWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.stack.StackWriteOptions;
import org.anchoranalysis.image.stack.Stack;

/**
 * A base class for a {@link StackWriter} delegates to another {@link StackWriter} based on values of a {@link StackWriteOptions}.  
 * 
 * @author Owen Feehan
 *
 */
public abstract class RasterWriterDelegateBase extends StackWriter {

    @Override
    public String fileExtension(StackWriteOptions writeOptions) {
        return selectDelegate(writeOptions).fileExtension(writeOptions);
    }

    @Override
    public void writeStack(
            Stack stack, Path filePath, boolean makeRGB, StackWriteOptions writeOptions)
            throws RasterIOException {
        selectDelegate(writeOptions).writeStack(stack, filePath, makeRGB, writeOptions);
    }

    @Override
    public void writeStackSeries(
            StackSeries stackSeries,
            Path filePath,
            boolean makeRGB,
            StackWriteOptions writeOptions)
            throws RasterIOException {
        selectDelegate(writeOptions).writeStackSeries(stackSeries, filePath, makeRGB, writeOptions);
    }

    /**
     * Selects a {@link StackWriter} to use as a delegate.
     * 
     * @param writeOptions options that specify what kind of rasters will be written.
     * @return the selected {@link StackWriter}
     */
    protected abstract StackWriter selectDelegate(StackWriteOptions writeOptions);
}
