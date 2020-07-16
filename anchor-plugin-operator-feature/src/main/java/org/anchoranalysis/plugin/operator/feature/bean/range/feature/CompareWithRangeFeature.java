/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range.feature;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.bean.range.CompareWithRange;
import org.anchoranalysis.plugin.operator.feature.bean.range.RangeCompareBase;

/**
 * Like {@link CompareWithRange} but uses features to calculate boundary values
 *
 * @author Owen Feehan
 * @param <T> feature-input type
 */
public class CompareWithRangeFeature<T extends FeatureInput> extends RangeCompareBase<T> {

    // START BEAN PROPERTIES
    /** Constant to return if value lies within the range */
    @BeanField private double withinValue = 0;

    /** Calculates minimally-allowed range boundary */
    @BeanField private Feature<T> min;

    /** Calculates maximally-allowed range boundary */
    @BeanField private Feature<T> max;
    // END BEAN PROPERTIES

    @Override
    protected Feature<T> featureToCalcInputVal() {
        return getItem();
    }

    @Override
    protected double boundaryMin(SessionInput<T> input) throws FeatureCalcException {
        return input.calc(min);
    }

    @Override
    protected double boundaryMax(SessionInput<T> input) throws FeatureCalcException {
        return input.calc(max);
    }

    @Override
    protected double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalcException {
        return withinValue;
    }

    @Override
    public String getParamDscr() {
        return String.format(
                "min=%s,max=%s,withinValue=%f,%s",
                min.getFriendlyName(), max.getFriendlyName(), withinValue, super.getParamDscr());
    }

    public Feature<T> getMin() {
        return min;
    }

    public void setMin(Feature<T> min) {
        this.min = min;
    }

    public Feature<T> getMax() {
        return max;
    }

    public void setMax(Feature<T> max) {
        this.max = max;
    }

    public double getWithinValue() {
        return withinValue;
    }

    public void setWithinValue(double withinValue) {
        this.withinValue = withinValue;
    }
}
