/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions;

import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Area of the stack (XY dimensions only, Z dimension is not considered)
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class Area<T extends FeatureInputNRG> extends FromDimensionsBase<T> {

    @Override
    protected double calcFromDims(ImageDimensions dim) {
        return dim.getVolumeXY();
    }
}
