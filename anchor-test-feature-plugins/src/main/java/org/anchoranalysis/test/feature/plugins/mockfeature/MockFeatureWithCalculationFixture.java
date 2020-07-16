/* (C)2020 */
package org.anchoranalysis.test.feature.plugins.mockfeature;

import static org.junit.Assert.assertEquals;

import java.util.function.ToDoubleFunction;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Creates a mock-feature which used a mock {@link org.anchoranalysis.feature.cache.calculation.FeatureCalculation} under the hood
 *
 * <p>Tests can be executed so as to count the number of times the calculation and feature's {@link MockFeatureWithCalculationFixture#calc) method are called.</p>
 *
 * <p>This is implemented using (ugly) static methods and some reflection, as Feature's must remain valid Anchor-bean's
 * and thus cannot be inner-classes or rely on parameterization in the constructor.</p>
 *
 * @author Owen Feehan
 *
 */
public class MockFeatureWithCalculationFixture {

    public static final ToDoubleFunction<FeatureInputSingleObject> DEFAULT_FUNC_NUM_PIXELS =
            input -> (double) input.getObject().numberVoxelsOn();

    @FunctionalInterface
    public interface RunnableWithException<E extends Exception> {
        public void run() throws E;
    }

    /**
     * Executes an operation, and afterwards asserts an expected number of times certain internal
     * methods are called on a mock-calculation.
     *
     * @param expectedCountCalc the expected number of calls to {@link Feature#calc}
     * @param expectedCountExecute the expected number of calls to {@code execute()} on the
     *     mock-calculation.
     * @param operation an operation, typically involving the mock-calculation.
     * @throws OperationFailedException
     */
    public static void executeAndAssertCount(
            int expectedCountCalc,
            int expectedCountExecute,
            RunnableWithException<OperationFailedException> operation)
            throws OperationFailedException {
        // We rely on static variables in the class, so no concurrency allowed
        synchronized (MockCalculation.class) {
            long beforeExecute = MockCalculation.countExecuteCalled;
            long beforeCalc = MockFeatureWithCalculation.countCalcCalled;

            operation.run();

            assertEquals(
                    "count of times execute() called on MockCalculation",
                    expectedCountExecute,
                    MockCalculation.countExecuteCalled - beforeExecute);

            assertEquals(
                    "count of times calc() called on MockFeature",
                    expectedCountCalc,
                    MockFeatureWithCalculation.countCalcCalled - beforeCalc);
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
     * @param childCache If set, a child-cache is used for the calculation. If not set, the main
     *     cache is used.
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
