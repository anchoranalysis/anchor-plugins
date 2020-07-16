/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.boundingbox;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class BoundingBoxNumberVoxels extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        return input.get().getObject().getBoundingBox().extent().getVolume();
    }
}
