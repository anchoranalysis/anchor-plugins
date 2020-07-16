/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions;

import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Calculates the volume (x, y and z dimensions) of the scene from the dimensions
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class Volume<T extends FeatureInputNRG> extends FromDimensionsBase<T> {

    @Override
    protected double calcFromDims(ImageDimensions dim) {
        return dim.getVolume();
    }
}
