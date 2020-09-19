package org.anchoranalysis.plugin.io.bean.rasterwriter;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import lombok.Getter;
import lombok.Setter;

/**
 * Uses different raster-writers under different sets of conditions.
 * 
 * @author Owen Feehan
 *
 */
public class MultiplexWriter {

    /** Default writer, if a more specific writer is not specified for a condition. */
    @BeanField @Getter @Setter private RasterWriter writer;
    
    /** Writer employed if a stack has more than one slice in z-dimension */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter if3D;
    
    /** Writer employed if a stack is grayscale */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter ifGrayscale;

    /** Writer employed if a stack is a three-channeled RGB image */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter ifRGB;

    
    
}
