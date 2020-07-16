/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.list;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public class Maximum<T extends FeatureInput> extends FeatureListElem<T> {

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        ListChecker.checkNonEmpty(getList());

        double maxValue = Double.NaN;
        for (Feature<T> f : getList()) {
            double val = input.calc(f);
            if (Double.isNaN(maxValue) || val > maxValue) {
                maxValue = val;
            }
        }
        return maxValue;
    }
}
