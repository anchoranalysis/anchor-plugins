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

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Clamps a feature value if it lies outside a specified range.
 *
 * <p>This class extends {@link RangeCompareFromScalars} to return particular constants if the
 * calculated feature value is below or above an acceptable range.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public class IfOutsideRange<T extends FeatureInput> extends RangeCompareFromScalars<T> {

    @Override
    protected Feature<T> featureToCalcInputValue() {
        return getItem();
    }

    @Override
    protected double withinRangeValue(double valWithinRange, FeatureCalculationInput<T> input)
            throws FeatureCalculationException {
        return valWithinRange;
    }
}
