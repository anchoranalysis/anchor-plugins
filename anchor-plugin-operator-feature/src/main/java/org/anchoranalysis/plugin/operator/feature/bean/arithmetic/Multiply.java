/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public class Multiply<T extends FeatureInput> extends FeatureListElem<T> {

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        double result = 1;

        for (Feature<T> elem : getList()) {
            result *= input.calc(elem);

            // Early exit if we start multiplying by 0
            if (result == 0) {
                return result;
            }
        }

        return result;
    }

    @Override
    public String getDscrLong() {
        return descriptionForList("*");
    }
}
