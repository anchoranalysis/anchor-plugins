/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;

/**
 * Expresses the number of intersecting pixels between two objects as a ratio to a denominator.
 *
 * <p>The denominator is calculated by the abstract method {@link #calculateDenominator}.
 *
 * @author Owen Feehan
 */
public abstract class OverlapRelative extends FeaturePairObjects {

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputPairObjects> input)
            throws FeatureCalculationException {

        FeatureInputPairObjects inputSessionless = input.get();

        return OverlapRatioUtilities.overlapRatioTo(
                inputSessionless, () -> calculateDenominator(inputSessionless));
    }

    /**
     * Calculates the denominator for the overlap ratio.
     *
     * @param input the {@link FeatureInputPairObjects} containing the two objects
     * @return the calculated denominator value
     */
    protected abstract int calculateDenominator(FeatureInputPairObjects input);
}
