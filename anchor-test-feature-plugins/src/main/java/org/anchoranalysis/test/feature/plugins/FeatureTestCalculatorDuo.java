/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;

/** Tests that consider two possibilities: positive and negative */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureTestCalculatorDuo {

    public static <T extends FeatureInput> void assertDoubleResult(
            String message,
            Feature<T> feature,
            T inputPositive,
            T inputNegative,
            double expectedResultPositive,
            double expectedResultNegative)
            throws FeatureCalculationException {
        FeatureTestCalculator.assertDoubleResult(
                positiveMessage(message), feature, inputPositive, expectedResultPositive);
        FeatureTestCalculator.assertDoubleResult(
                negativeMessage(message), feature, inputNegative, expectedResultNegative);
    }

    public static <T extends FeatureInput> void assertIntResult(
            String message,
            Feature<T> feature,
            T inputPositive,
            T inputNegative,
            int expectedResultPositive,
            int expectedResultNegative)
            throws FeatureCalculationException {
        FeatureTestCalculator.assertIntResult(
                positiveMessage(message), feature, inputPositive, expectedResultPositive);
        FeatureTestCalculator.assertIntResult(
                negativeMessage(message), feature, inputNegative, expectedResultNegative);
    }

    private static String positiveMessage(String message) {
        return message + " (positive case)";
    }

    private static String negativeMessage(String message) {
        return message + " (negative case)";
    }
}
