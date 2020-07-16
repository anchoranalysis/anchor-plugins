/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range.feature;

import org.anchoranalysis.feature.bean.operator.FeatureDoubleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * The range of two feature values (i.e. max - min), divided by their mean.
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class NormalizedRange<T extends FeatureInput> extends FeatureDoubleElem<T> {

    // START BEAN PROPERTIES
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        double val1 = input.calc(getItem1());
        double val2 = input.calc(getItem2());

        double absDiff = Math.abs(val1 - val2);
        double mean = (val1 + val2) / 2;

        if (mean == 0.0) {
            return 0.0;
        }

        return absDiff / mean;
    }

    @Override
    public String getDscrLong() {
        return String.format("%s - %s", getItem1().getDscrLong(), getItem2().getDscrLong());
    }
}
