/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

public class ShapeRegularityCenterSlice extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        return ShapeRegularityCalculator.calcShapeRegularity(centerSlice(input.get().getObject()));
    }

    private ObjectMask centerSlice(ObjectMask object) {
        int zSliceCenter = (int) object.centerOfGravity().getZ();
        return object.extractSlice(
                zSliceCenter - object.getBoundingBox().cornerMin().getZ(), false);
    }
}
