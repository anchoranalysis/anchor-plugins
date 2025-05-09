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

package org.anchoranalysis.plugin.image.feature.bean.dimensions;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputDimensions;
import org.anchoranalysis.image.core.dimensions.Dimensions;

/**
 * Base class for features that are calculated from {@link Dimensions}.
 *
 * @param <T> feature-input-type that extends {@link FeatureInputDimensions}
 */
public abstract class FromDimensionsBase<T extends FeatureInputDimensions> extends Feature<T> {

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {
        return calculateFromDimensions(input.get().dimensions());
    }

    @Override
    public Class<? extends FeatureInput> inputType() {
        return FeatureInputDimensions.class;
    }

    /**
     * Calculates a feature value from the given dimensions.
     *
     * @param dimensions the {@link Dimensions} to calculate from
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateFromDimensions(Dimensions dimensions)
            throws FeatureCalculationException;
}
