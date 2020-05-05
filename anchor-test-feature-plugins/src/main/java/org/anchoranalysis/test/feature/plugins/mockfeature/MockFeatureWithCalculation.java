package org.anchoranalysis.test.feature.plugins.mockfeature;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;

/**
 * A feature that returns the number-of-voxels in an object by using a {@link MockCalculation} internally
 * 
 * @author Owen Feehan
 *
 */
class MockFeatureWithCalculation extends Feature<FeatureInput> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// Incremented every calc executed is called
	static int cntCalcCalled = 0;
	
	@Override
	protected double calc(SessionInput<FeatureInput> input) throws FeatureCalcException {
		cntCalcCalled++;
		MockCalculation calculation = new MockCalculation();
		return input.calc( calculation );	
	}

	@Override
	public FeatureInputDescriptor paramType() throws FeatureCalcException {
		throw new FeatureCalcException("This method is not supported in this mock");
	}
}