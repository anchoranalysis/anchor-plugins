package org.anchoranalysis.plugin.image.bean.dimensions.provider;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.spatial.Extent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Specify dimensions via a bean.
 * 
 * <p>No resolution information is assigned.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor
public class SpecifyDimensions extends DimensionsProvider {

    // START BEAN PROPERTIES
    /** Length of X and Y dimensions. */
    @BeanField @Getter @Setter SizeXY sizeXY;
    
    /** Length of Z dimension. */
    @BeanField @Getter @Setter int sizeZ = 1;
    // END BEAN PROPERTIES
    
    /**
     * Create with specific lengths for X and Y dimensions.
     * 
     * @param sizeXY the size
     */
    public SpecifyDimensions(SizeXY sizeXY) {
        this.sizeXY = sizeXY;
    }
    
    @Override
    public Dimensions create() throws CreateException {
        Extent extent = sizeXY.asExtent(sizeZ);
        return new Dimensions(extent, Optional.empty());
    }
}
