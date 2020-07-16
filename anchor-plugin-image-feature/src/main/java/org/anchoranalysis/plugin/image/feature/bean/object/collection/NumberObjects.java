/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.collection;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.collection.FeatureObjectCollection;
import org.anchoranalysis.image.feature.object.input.FeatureInputObjectCollection;

public class NumberObjects extends FeatureObjectCollection {

    @Override
    public double calc(FeatureInputObjectCollection params) throws FeatureCalcException {
        return params.getObjects().size();
    }
}
