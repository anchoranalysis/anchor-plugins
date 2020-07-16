/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class ShapeRegularityMIP extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        // Maximum-intensity projection of the mask
        return ShapeRegularityCalculator.calcShapeRegularity(
                input.get().getObject().duplicate().flattenZ());
    }
}
