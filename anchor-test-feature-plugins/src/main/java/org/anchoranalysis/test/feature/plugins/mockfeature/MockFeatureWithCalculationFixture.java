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

package org.anchoranalysis.test.feature.plugins.mockfeature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.ToDoubleFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedRunnable;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * Creates a mock-feature which used a mock {@link
 * org.anchoranalysis.feature.calculate.part.CalculationPart} under the hood
 *
 * <p>Tests can be executed so as to count the number of times the calculation and feature's {@link
 * MockFeatureWithCalculation#calculate} method are called.
 *
 * <p>This is implemented using (ugly) static methods and some reflection, as Feature's must remain
 * valid Anchor-bean's and thus cannot be inner-classes or rely on parameterization in the
 * constructor.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockFeatureWithCalculationFixture {

    public static final ToDoubleFunction<FeatureInputSingleObject> DEFAULT_FUNC_NUM_PIXELS =
            input -> (double) input.getObject().numberVoxelsOn();

    /**
     * Executes an operation, and afterwards asserts an expected number of times certain internal
     * methods are called on a mock-calculation.
     *
     * @param expectedCountCalc the expected number of calls to {@link Feature#calculate}
     * @param expectedCountExecute the expected number of calls to {@code execute()} on the
     *     mock-calculation.
     * @param operation an operation, typically involving the mock-calculation.
     * @throws OperationFailedException
     */
    public static void executeAndAssertCount(
            int expectedCountCalc,
            int expectedCountExecute,
            CheckedRunnable<OperationFailedException> operation)
            throws OperationFailedException {
        // We rely on static variables in the class, so no concurrency allowed
        synchronized (MockCalculation.class) {
            long beforeExecute = MockCalculation.countExecuteCalled;
            long beforeCalc = MockFeatureWithCalculation.countCalculateCalled;

            operation.run();

            assertEquals(
                    expectedCountExecute,
                    MockCalculation.countExecuteCalled - beforeExecute,
                    "count of times execute() called on MockCalculation");

            assertEquals(
                    expectedCountCalc,
                    MockFeatureWithCalculation.countCalculateCalled - beforeCalc,
                    "count of times calculate() called on MockFeature");
        }
    }

    /**
     * Creates a mock-feature (using a mock-FeatureCalculation under the hood) which counts the
     * number of pixels in an object
     *
     * @return the mock-feature
     */
    public static Feature<FeatureInputSingleObject> createMockFeatureWithCalculation() {
        return createMockFeatureWithCalculation(DEFAULT_FUNC_NUM_PIXELS);
    }

    /**
     * Creates a mock-feature (using a mock-FeatureCalculation under the hood) with a result
     * calculated using a lambda
     *
     * @param funcCalculation lambda that provides the result of the mock-calculation (and this
     *     mock-feature)
     * @return the feature
     */
    @SuppressWarnings("unchecked")
    public static <T extends FeatureInput> Feature<T> createMockFeatureWithCalculation(
            ToDoubleFunction<T> funcCalculation) {
        synchronized (MockCalculation.class) {
            MockCalculation.funcCalculation = (ToDoubleFunction<FeatureInput>) funcCalculation;
            return (Feature<T>) new MockFeatureWithCalculation();
        }
    }
}
