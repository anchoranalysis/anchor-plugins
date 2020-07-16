/* (C)2020 */
package org.anchoranalysis.test.feature.plugins.mockfeature;

import java.util.function.ToDoubleFunction;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Counts the number of pixels in the object, but uses a static variable to record the number of
 * times executed is called
 */
class MockCalculation extends FeatureCalculation<Double, FeatureInput> {

    // Incremented every time executed is called
    static int countExecuteCalled = 0;

    /** Can be changed from the default implementation as desired */
    static ToDoubleFunction<FeatureInput> funcCalculation;

    @Override
    protected Double execute(FeatureInput input) throws FeatureCalcException {
        assert (funcCalculation != null); // NOSONAR
        countExecuteCalled++; // NOSONAR
        return funcCalculation.applyAsDouble(input); // NOSONAR
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MockCalculation;
    }

    @Override
    public int hashCode() {
        return 7;
    }
}
