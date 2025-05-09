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

package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.bean.WithValueBase;

/**
 * A feature that multiplies the result of another feature by a constant value.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
@NoArgsConstructor
public class MultiplyByConstant<T extends FeatureInput> extends WithValueBase<T> {

    /**
     * Creates a new {@link MultiplyByConstant} with a specified feature and constant value.
     *
     * @param feature the {@link Feature} to be multiplied
     * @param value the constant value to multiply by
     */
    public MultiplyByConstant(Feature<T> feature, double value) {
        setItem(feature);
        setValue(value);
    }

    @Override
    protected double combineValueAndFeature(double value, double featureResult) {
        return value * featureResult;
    }

    @Override
    protected String combineDescription(String valueDescription, String featureDescription) {
        return valueDescription + " * " + featureDescription;
    }
}
