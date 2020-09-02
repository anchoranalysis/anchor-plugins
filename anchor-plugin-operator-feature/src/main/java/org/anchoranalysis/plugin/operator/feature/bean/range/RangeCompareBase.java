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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;

public abstract class RangeCompareBase<T extends FeatureInput> extends FeatureUnaryGeneric<T> {

    // START BEAN PROPERTIES
    /** Constant to return if outside the range (below the minimum allowed) */
    @BeanField @Getter @Setter private double belowMinValue = 0;

    /** Constant to return if outside the range (above the maximum allowed) */
    @BeanField @Getter @Setter private double aboveMaxValue = 0;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<T> input) throws FeatureCalculationException {
        return calculateForValue(input.calculate(featureToCalcInputVal()), input);
    }

    /** Boundary to define the minimum accepted value in the range */
    protected abstract double boundaryMin(SessionInput<T> input) throws FeatureCalculationException;

    /** Boundary to define the maximum accepted value in the range */
    protected abstract double boundaryMax(SessionInput<T> input) throws FeatureCalculationException;

    /**
     * Which feature to calculate the input-value? The result is then passed to {@link
     * #calculateForValue}
     */
    protected abstract Feature<T> featureToCalcInputVal();

    /** What value to return if the value is inside the range */
    protected abstract double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalculationException;

    /**
     * Calculates for an input-value, return constant values if its outside the range, or otherwise
     * calling a function
     *
     * @param val input-value
     * @return either a constant-value if outside the range, or else the result of the
     *     withinRangeValue function
     * @throws FeatureCalculationException
     */
    private double calculateForValue(double val, SessionInput<T> input)
            throws FeatureCalculationException {

        if (val < boundaryMin(input)) {
            return belowMinValue;
        }

        if (val > boundaryMax(input)) {
            return aboveMaxValue;
        }

        return withinRangeValue(val, input);
    }

    @Override
    public String describeParams() {
        return String.format("belowMinValue=%f,aboveMaxValue=%f", belowMinValue, aboveMaxValue);
    }
}
