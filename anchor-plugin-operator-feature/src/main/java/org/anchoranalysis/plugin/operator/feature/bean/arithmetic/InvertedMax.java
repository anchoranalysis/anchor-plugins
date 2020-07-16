/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Finds the repciprocal (multiplicate inverse), but imposes a maximum ceiling via a constant.
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class InvertedMax<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PARAMETERS
    @BeanField private double max = 100;
    // END BEAN PARAMETERS

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return Math.min((1 / input.calc(getItem())), max);
    }

    @Override
    public String getParamDscr() {
        return String.format("inverted_max=%f", max);
    }

    @Override
    public String getDscrLong() {
        return "(1/" + getItem().getBeanName() + ")[max=" + max + "]";
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
