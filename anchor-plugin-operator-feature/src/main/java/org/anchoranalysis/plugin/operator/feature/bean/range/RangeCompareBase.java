/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public abstract class RangeCompareBase<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIES
    /** Constant to return if outside the range (below the minimum allowed) */
    @BeanField private double belowMinValue = 0;

    /** Constant to return if outside the range (above the maximum allowed) */
    @BeanField private double aboveMaxValue = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return calcForVal(input.calc(featureToCalcInputVal()), input);
    }

    /** Boundary to define the minimum accepted value in the range */
    protected abstract double boundaryMin(SessionInput<T> input) throws FeatureCalcException;

    /** Boundary to define the maximum accepted value in the range */
    protected abstract double boundaryMax(SessionInput<T> input) throws FeatureCalcException;

    /**
     * Which feature to calculate the input-value? The result is then passed to {@link #calcForVal}
     */
    protected abstract Feature<T> featureToCalcInputVal();

    /** What value to return if the value is inside the range */
    protected abstract double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalcException;

    /**
     * Calculates for an input-value, return constant values if its outside the range, or otherwise
     * calling a function
     *
     * @param val input-value
     * @return either a constant-value if outside the range, or else the result of the
     *     withinRangeValue function
     * @throws FeatureCalcException
     */
    private double calcForVal(double val, SessionInput<T> input) throws FeatureCalcException {

        if (val < boundaryMin(input)) {
            return belowMinValue;
        }

        if (val > boundaryMax(input)) {
            return aboveMaxValue;
        }

        return withinRangeValue(val, input);
    }

    @Override
    public String getParamDscr() {
        return String.format("belowMinValue=%f,aboveMaxValue=%f", belowMinValue, aboveMaxValue);
    }

    public double getBelowMinValue() {
        return belowMinValue;
    }

    public void setBelowMinValue(double belowMinValue) {
        this.belowMinValue = belowMinValue;
    }

    public double getAboveMaxValue() {
        return aboveMaxValue;
    }

    public void setAboveMaxValue(double aboveMaxValue) {
        this.aboveMaxValue = aboveMaxValue;
    }
}
