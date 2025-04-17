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

    /**
     * A default function for counting the number of pixels (voxels) in a single object.
     * 
     * <p>This function takes a {@link FeatureInputSingleObject} as input and returns
     * the number of "on" voxels in the object as a double value. It is used as the
     * default calculation function when creating a mock feature without specifying
     * a custom function.</p>
     */
    public static final ToDoubleFunction<FeatureInputSingleObject> DEFAULT_FUNC_NUM_PIXELS =
            input -> (double) input.getObject().numberVoxelsOn();

    /**
     * Executes an operation and asserts the expected number of calls to internal methods.
     *
     * <p>This method runs the given operation and then checks if the number of calls to
     * {@link Feature#calculate} and the mock calculation's {@code execute()} method match
     * the expected counts.</p>
     *
     * @param expectedCountCalc the expected number of calls to {@link Feature#calculate}
     * @param expectedCountExecute the expected number of calls to {@code execute()} on the mock calculation
     * @param operation the operation to execute, typically involving the mock calculation
     * @throws OperationFailedException if the operation fails
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
     * Creates a mock feature that counts the number of pixels in an object.
     *
     * <p>This mock feature uses a mock {@link org.anchoranalysis.feature.calculate.part.CalculationPart}
     * under the hood to perform the pixel counting.</p>
     *
     * @return a new {@link Feature} that counts pixels in a {@link FeatureInputSingleObject}
     */
    public static Feature<FeatureInputSingleObject> createMockFeatureWithCalculation() {
        return createMockFeatureWithCalculation(DEFAULT_FUNC_NUM_PIXELS);
    }

    /**
     * Creates a mock feature with a custom calculation function.
     *
     * <p>This method creates a mock feature that uses the provided function to calculate its result.
     * It uses a mock {@link org.anchoranalysis.feature.calculate.part.CalculationPart} under the hood.</p>
     *
     * @param <T> the type of {@link FeatureInput} for the feature
     * @param funcCalculation a function that calculates the result for the mock feature
     * @return a new {@link Feature} that uses the provided calculation function
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