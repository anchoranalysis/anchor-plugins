/*-
 * #%L
 * anchor-plugin-operator-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.operator.feature.bean.range;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureUnaryGeneric;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for features that compare a value against a range and return different results based
 * on the comparison.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public abstract class RangeCompareBase<T extends FeatureInput> extends FeatureUnaryGeneric<T> {

    // START BEAN PROPERTIES
    /** Constant to return if outside the range (below the minimum allowed) */
    @BeanField @Getter @Setter private double belowMinValue = 0;

    /** Constant to return if outside the range (above the maximum allowed) */
    @BeanField @Getter @Setter private double aboveMaxValue = 0;
    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {
        return calculateForValue(input.calculate(featureToCalcInputValue()), input);
    }

    /**
     * Defines the minimum accepted value in the range.
     *
     * @param input the {@link FeatureCalculationInput} to use for calculation
     * @return the minimum boundary value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double boundaryMin(FeatureCalculationInput<T> input)
            throws FeatureCalculationException;

    /**
     * Defines the maximum accepted value in the range.
     *
     * @param input the {@link FeatureCalculationInput} to use for calculation
     * @return the maximum boundary value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double boundaryMax(FeatureCalculationInput<T> input)
            throws FeatureCalculationException;

    /**
     * Specifies which feature to calculate the input value.
     *
     * @return the {@link Feature} used to calculate the input value
     */
    protected abstract Feature<T> featureToCalcInputValue();

    /**
     * Calculates the value to return if the input is within the range.
     *
     * @param valWithinRange the input value that is within the range
     * @param input the {@link FeatureCalculationInput} to use for calculation
     * @return the calculated value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double withinRangeValue(
            double valWithinRange, FeatureCalculationInput<T> input)
            throws FeatureCalculationException;

    /**
     * Calculates the result based on an input value, returning constant values if it's outside the
     * range.
     *
     * @param value input value
     * @param input the {@link FeatureCalculationInput} to use for calculation
     * @return either a constant value if outside the range, or the result of the withinRangeValue
     *     function
     * @throws FeatureCalculationException if the calculation fails
     */
    private double calculateForValue(double value, FeatureCalculationInput<T> input)
            throws FeatureCalculationException {

        if (value < boundaryMin(input)) {
            return belowMinValue;
        }

        if (value > boundaryMax(input)) {
            return aboveMaxValue;
        }

        return withinRangeValue(value, input);
    }

    @Override
    public String describeParameters() {
        return String.format("belowMinValue=%f,aboveMaxValue=%f", belowMinValue, aboveMaxValue);
    }
}
