package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import lombok.Getter;
import lombok.Setter;

/**
 * Uses different raster-writers under different sets of conditions.
 * 
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case.
 * 
 * @author Owen Feehan
 *
 */
public class MultiplexWriter extends RasterWriter {

    /** Default writer, if a more specific writer is not specified for a condition. */
    @BeanField @Getter @Setter private RasterWriter writer;
    
    /** Writer employed if a stack is a one or three-channeled image, that is <b>not 3D</b>, and not RGB. */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenOneOrThreeChannels;
    
    /** Writer employed if a stack is a three-channeled RGB image and is <b>not 3D</b>. */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenRGB;

    @Override
    public String fileExtension(RasterWriteOptions writeOptions) {
        return selectDelegate(writeOptions).fileExtension(writeOptions);
    }

    @Override
    public void writeStack(Stack stack, Path filePath, boolean makeRGB,
            RasterWriteOptions writeOptions) throws RasterIOException {
        selectDelegate(writeOptions).writeStack(stack, filePath, makeRGB, writeOptions);
    }

    @Override
    public void writeStackSeries(StackSeries stackSeries, Path filePath, boolean makeRGB,
            RasterWriteOptions writeOptions) throws RasterIOException {
        selectDelegate(writeOptions).writeStackSeries(stackSeries, filePath, makeRGB, writeOptions);
    }
    
    private RasterWriter selectDelegate(RasterWriteOptions writeOptions) {
        if (writeOptions.isAlways2D()) {
            
            if (writeOptions.isRgb()) {
                return whenRGB;
            } else if (writeOptions.isAlwaysOneOrThreeChannels()) {
                return whenOneOrThreeChannels;
            } else {
                return writer;
            }
            
        } else {
            return writer;
        }
    }
}
