/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Compares a value with a range, returning specified constants if its inside the range, below it or
 * above it
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class CompareWithRange<T extends FeatureInput> extends RangeCompareFromScalars<T> {

    // START BEAN PROPERTIES
    /** Constant to return if value lies within the range */
    @BeanField private double withinValue = 0;
    // END BEAN PROPERTIES

    @Override
    protected Feature<T> featureToCalcInputVal() {
        return getItem();
    }

    @Override
    protected double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalcException {
        return withinValue;
    }

    @Override
    public String getParamDscr() {
        return String.format("%s,withinValue=%f", super.getParamDscr(), withinValue);
    }

    public double getWithinValue() {
        return withinValue;
    }

    public void setWithinValue(double withinValue) {
        this.withinValue = withinValue;
    }
}
