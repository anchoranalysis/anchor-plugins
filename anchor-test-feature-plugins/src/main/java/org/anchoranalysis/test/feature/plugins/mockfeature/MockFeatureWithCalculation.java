/* (C)2020 */
package org.anchoranalysis.test.feature.plugins.mockfeature;

import org.anchoranalysis.feature.bean.operator.FeatureOperator;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A feature that returns the number-of-voxels in an object by using am internal mock-calculation.
 *
 * <p>Warning! Method calls are counted via ugly static variable implementation.
 *
 * @author Owen Feehan
 */
public class MockFeatureWithCalculation extends FeatureOperator<FeatureInput> {

    /**
     * Records a count of the number of calls to {@link #calc}
     *
     * <p>It is incremented every time {@link #calc} is called.
     */
    static int countCalcCalled = 0;

    @Override
    protected double calc(SessionInput<FeatureInput> input) throws FeatureCalcException {
        countCalcCalled++; // NOSONAR
        return input.calc(new MockCalculation());
    }
}
