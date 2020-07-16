/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.list;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Arithmetic mean of a list of features
 *
 * @author Owen Feehan
 * @param <T> feature-input-type for all features in the list
 */
public class Mean<T extends FeatureInput> extends FeatureListElem<T> {

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        double result = 0;

        ListChecker.checkNonEmpty(getList());

        for (Feature<T> elem : getList()) {
            result += input.calc(elem);
        }

        assert (!Double.isNaN(result));

        return result / getList().size();
    }
}
