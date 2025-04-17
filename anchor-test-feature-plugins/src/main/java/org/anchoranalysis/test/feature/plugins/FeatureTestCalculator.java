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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.shared.SharedFeatures;
import org.anchoranalysis.test.LoggerFixture;

/**
 * Utility class for testing feature calculations in a controlled environment.
 *
 * <p>This class provides methods to assert the results of feature calculations
 * against expected values, supporting both double and integer results.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureTestCalculator {

    /**
     * Asserts that a feature calculation results in an expected double value.
     *
     * @param <T> the type of feature input
     * @param message the assertion message
     * @param feature the feature to calculate
     * @param input the input for the feature calculation
     * @param expectedResult the expected result of the calculation
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static <T extends FeatureInput> void assertDoubleResult(
            String message, Feature<T> feature, T input, double expectedResult)
            throws FeatureCalculationException {
        assertDoubleResult(message, feature, input, Optional.empty(), expectedResult);
    }

    /**
     * Asserts that a feature calculation results in an expected integer value.
     *
     * @param <T> the type of feature input
     * @param message the assertion message
     * @param feature the feature to calculate
     * @param input the input for the feature calculation
     * @param expectedResult the expected result of the calculation
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static <T extends FeatureInput> void assertIntResult(
            String message, Feature<T> feature, T input, int expectedResult)
            throws FeatureCalculationException {
        assertIntResult(message, feature, input, Optional.empty(), expectedResult);
    }

    /**
     * Asserts that a feature calculation results in an expected double value, with shared objects.
     *
     * @param <T> the type of feature input
     * @param message the assertion message
     * @param feature the feature to calculate
     * @param input the input for the feature calculation
     * @param sharedObjects optional shared objects for the calculation
     * @param expectedResult the expected result of the calculation
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static <T extends FeatureInput> void assertDoubleResult(
            String message,
            Feature<T> feature,
            T input,
            Optional<SharedObjects> sharedObjects,
            double expectedResult)
            throws FeatureCalculationException {
        assertResultTolerance(
                message, feature, input, createInitialization(sharedObjects), expectedResult, 1e-4);
    }

    /**
     * Asserts that a feature calculation results in an expected integer value, with shared objects.
     *
     * @param <T> the type of feature input
     * @param message the assertion message
     * @param feature the feature to calculate
     * @param input the input for the feature calculation
     * @param sharedObjects optional shared objects for the calculation
     * @param expectedResult the expected result of the calculation
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static <T extends FeatureInput> void assertIntResult(
            String message,
            Feature<T> feature,
            T input,
            Optional<SharedObjects> sharedObjects,
            int expectedResult)
            throws FeatureCalculationException {
        assertResultTolerance(
                message,
                feature,
                input,
                createInitialization(sharedObjects),
                expectedResult,
                1e-20);
    }

    private static FeatureInitialization createInitialization(
            Optional<SharedObjects> sharedObjects) {
        Optional<FeatureInitialization> mapped = sharedObjects.map(FeatureInitialization::new);
        return mapped.orElse(new FeatureInitialization());
    }

    private static <T extends FeatureInput> void assertResultTolerance(
            String message,
            Feature<T> feature,
            T input,
            FeatureInitialization initialization,
            double expectedResult,
            double delta)
            throws FeatureCalculationException {
        double result =
                FeatureTestCalculator.calculateSequentialSession(feature, input, initialization);
        assertEquals(expectedResult, result, delta, message);
    }

    private static <T extends FeatureInput> double calculateSequentialSession(
            Feature<T> feature, T input, FeatureInitialization initialization)
            throws FeatureCalculationException {

        try {
            FeatureCalculatorSingle<T> calculator =
                    FeatureSession.with(
                            feature,
                            initialization,
                            new SharedFeatures(),
                            LoggerFixture.suppressedLogger());

            return calculator.calculate(input);
        } catch (InitializeException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
