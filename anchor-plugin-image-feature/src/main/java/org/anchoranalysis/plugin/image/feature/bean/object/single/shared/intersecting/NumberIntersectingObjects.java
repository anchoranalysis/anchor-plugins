/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

public class NumberIntersectingObjects extends FeatureIntersectingObjects {

    // START BEAN PROPERTIES
    // END BEAN PROPERTIES

    @Override
    protected double valueFor(
            SessionInput<FeatureInputSingleObject> params,
            ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting)
            throws FeatureCalcException {
        return params.calc(intersecting).size();
    }
}
