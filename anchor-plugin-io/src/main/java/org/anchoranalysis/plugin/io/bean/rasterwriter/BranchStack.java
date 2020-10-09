package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import lombok.Getter;
import lombok.Setter;

/**
 * Uses different raster-writers depending on whether it is always 2D (not a z-stack) or possibly 3D.
 *
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case.
 *
 * @author Owen Feehan
 */
public class BranchStack extends RasterWriterDelegateBase {

    // START BEAN PROPERTIES
    /** Writer to use if it is guaranteed that the image will always be 2D. */
    @BeanField @Getter @Setter private RasterWriter writerAlways2D;
    
    /** Otherwise the writer to use. */
    @BeanField @Getter @Setter private RasterWriter writerElse;
    // END BEAN PROPERTIES
    
    @Override
    protected RasterWriter selectDelegate(RasterWriteOptions writeOptions) {
        if (writeOptions.isAlways2D()) {
            return writerAlways2D;
        } else {
            return writerElse;
        }
    }
}
