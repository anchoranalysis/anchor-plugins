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

/**
 * Utility class for testing features with both positive and negative cases.
 *
 * <p>This class provides methods to assert the results of feature calculations for both positive
 * and negative inputs, supporting both double and integer results.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureTestCalculatorDuo {

    /**
     * Asserts that a feature calculation results in expected double values for both positive and
     * negative inputs.
     *
     * @param <T> the type of feature input
     * @param message the base assertion message
     * @param feature the feature to calculate
     * @param inputPositive the positive input for the feature calculation
     * @param inputNegative the negative input for the feature calculation
     * @param expectedResultPositive the expected result for the positive input
     * @param expectedResultNegative the expected result for the negative input
     * @throws FeatureCalculationException if the feature calculation fails
     */
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

    /**
     * Asserts that a feature calculation results in expected integer values for both positive and
     * negative inputs.
     *
     * @param <T> the type of feature input
     * @param message the base assertion message
     * @param feature the feature to calculate
     * @param inputPositive the positive input for the feature calculation
     * @param inputNegative the negative input for the feature calculation
     * @param expectedResultPositive the expected result for the positive input
     * @param expectedResultNegative the expected result for the negative input
     * @throws FeatureCalculationException if the feature calculation fails
     */
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
