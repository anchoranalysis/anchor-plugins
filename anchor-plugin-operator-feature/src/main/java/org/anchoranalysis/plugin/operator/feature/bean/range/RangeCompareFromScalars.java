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
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for setting the boundaries of a range using constant scalar values.
 *
 * <p>This class extends {@link RangeCompareBase} to provide a mechanism for defining a range with
 * fixed minimum and maximum values.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public abstract class RangeCompareFromScalars<T extends FeatureInput> extends RangeCompareBase<T> {

    // START BEAN PROPERTIES
    /**
     * Boundary value indicating the minimally-allowed range value. Default value is {@link
     * Double#NEGATIVE_INFINITY}.
     */
    @BeanField @Getter @Setter private double min = Double.NEGATIVE_INFINITY;

    /**
     * Boundary value indicating the maximally-allowed range value. Default value is {@link
     * Double#POSITIVE_INFINITY}.
     */
    @BeanField @Getter @Setter private double max = Double.POSITIVE_INFINITY;
    // END BEAN PROPERTIES

    @Override
    public String describeParameters() {
        return String.format("min=%f,max=%f,%s", min, max, super.describeParameters());
    }

    @Override
    protected double boundaryMin(FeatureCalculationInput<T> input) {
        return min;
    }

    @Override
    protected double boundaryMax(FeatureCalculationInput<T> input) {
        return max;
    }
}
