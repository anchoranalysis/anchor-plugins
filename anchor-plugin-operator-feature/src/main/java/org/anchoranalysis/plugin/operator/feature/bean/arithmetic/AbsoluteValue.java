/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public class AbsoluteValue<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIES
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return Math.abs(input.calc(getItem()));
    }

    @Override
    public String getDscrLong() {
        return String.format("abs(%s)", getItem().getDscrLong());
    }
}
