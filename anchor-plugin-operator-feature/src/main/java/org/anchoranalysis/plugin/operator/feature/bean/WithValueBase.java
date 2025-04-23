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

package org.anchoranalysis.plugin.operator.feature.bean;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureUnaryGeneric;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for operations that use a specified constant value in combination with a feature.
 *
 * <p>This abstract class extends {@link FeatureUnaryGeneric} to provide a framework for combining a
 * constant value with the result of a feature calculation.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public abstract class WithValueBase<T extends FeatureInput> extends FeatureUnaryGeneric<T> {

    /** The constant value to be used in the operation. Default value is 0. */
    @BeanField @Getter @Setter private double value = 0;

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {
        return combineValueAndFeature(value, input.calculate(getItem()));
    }

    @Override
    public String descriptionLong() {
        return combineDescription(String.format("%f", value), getItem().descriptionLong());
    }

    /**
     * Combines the constant value with the feature result.
     *
     * @param value the constant value specified for this operation
     * @param featureResult the result of the feature calculation
     * @return the combined result
     */
    protected abstract double combineValueAndFeature(double value, double featureResult);

    /**
     * Combines the descriptions of the constant value and the feature.
     *
     * @param valueDescription the description of the constant value
     * @param featureDescription the description of the feature
     * @return the combined description
     */
    protected abstract String combineDescription(
            String valueDescription, String featureDescription);
}
