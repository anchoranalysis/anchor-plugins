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
import org.anchoranalysis.feature.bean.operator.FeatureBinary;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Calculates the statistical range between two feature values, with optional weighting.
 *
 * <p>The range is calculated as the absolute difference between two feature values. Different
 * weights can be applied depending on which value is greater.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public class Range<T extends FeatureInput> extends FeatureBinary<T> {

    /**
     * Weight applied when the first item's value is greater than the second. Default value is 1.0.
     */
    @BeanField @Getter @Setter private double weightItem1Greater = 1.0;

    /**
     * Weight applied when the second item's value is greater than the first. Default value is 1.0.
     */
    @BeanField @Getter @Setter private double weightItem2Greater = 1.0;

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {

        double val1 = input.calculate(getItem1());
        double val2 = input.calculate(getItem2());

        if (val1 == val2) {
            return 0.0;
        } else if (val1 > val2) {
            return weightItem1Greater * (val1 - val2);
        } else {
            return weightItem2Greater * (val2 - val1);
        }
    }

    @Override
    public String descriptionLong() {
        return String.format("%s - %s", getItem1().descriptionLong(), getItem2().descriptionLong());
    }
}
