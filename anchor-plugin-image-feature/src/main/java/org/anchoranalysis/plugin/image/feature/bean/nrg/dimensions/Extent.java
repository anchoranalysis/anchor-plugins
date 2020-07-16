/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions;

import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Width/height/depth of nrg-stack in pixels depending on axis
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class Extent<T extends FeatureInputNRG> extends ForSpecificAxis<T> {

    @Override
    protected double calcForAxis(ImageDimensions dimensions, AxisType axis) {
        return dimensions.getExtent().getValueByDimension(axis);
    }
}
