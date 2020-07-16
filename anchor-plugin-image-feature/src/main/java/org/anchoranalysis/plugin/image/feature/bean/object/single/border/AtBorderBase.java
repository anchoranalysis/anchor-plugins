/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.border;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public abstract class AtBorderBase extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        if (isInputAtBorder(input.get())) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private boolean isInputAtBorder(FeatureInputSingleObject input) throws FeatureCalcException {
        return isBoundingBoxAtBorder(
                input.getObject().getBoundingBox(), input.getDimensionsRequired());
    }

    protected abstract boolean isBoundingBoxAtBorder(
            BoundingBox boundingBox, ImageDimensions dimensions);
}
