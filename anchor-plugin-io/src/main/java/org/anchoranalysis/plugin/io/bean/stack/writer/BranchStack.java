package org.anchoranalysis.plugin.io.bean.stack.writer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.bean.stack.StackWriter;
import org.anchoranalysis.image.io.stack.StackWriteOptions;
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
    @BeanField @Getter @Setter private StackWriter writerAlways2D;
    
    /** Otherwise the writer to use. */
    @BeanField @Getter @Setter private StackWriter writerElse;
    // END BEAN PROPERTIES
    
    @Override
    protected StackWriter selectDelegate(StackWriteOptions writeOptions) {
        if (writeOptions.isAlways2D()) {
            return writerAlways2D;
        } else {
            return writerElse;
        }
    }
}
