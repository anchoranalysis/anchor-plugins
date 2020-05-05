package org.anchoranalysis.test.feature.plugins.mockfeature;

import java.util.function.Function;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/** Counts the number of pixels in the object, but uses a static variable to record the number of times executed is called */
class MockCalculation extends FeatureCalculation<Double,FeatureInput> {

	// Incremented every time executed is called
	static int cntExecuteCalled = 0;
	
	/** Can be changed from the default implementation as desired */
	static Function<FeatureInput,Double> funcCalculation;
	
	@Override
	protected Double execute(FeatureInput input) throws FeatureCalcException {
		assert(funcCalculation!=null);
		cntExecuteCalled++;
		return funcCalculation.apply(input);
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