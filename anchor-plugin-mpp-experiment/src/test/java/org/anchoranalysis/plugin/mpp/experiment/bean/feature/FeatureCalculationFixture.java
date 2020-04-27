package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

class FeatureCalculationFixture {

	@FunctionalInterface
	public interface RunnableWithException<E extends Throwable> {
	    public void run() throws E;
	}
	
	/**
	 * Executes an operation, and afterwards asserts an expected number of times the {@link MockCalculation.execute()} method was called.
	 * 
	 * @param expectedCnt the expected number of calls to {@link MockCalculation.execute()}
	 * @param operation an operation, typically involving {@link MockCalculation}
	 * @throws OperationFailedException
	 */
	public static void executeAndAssertCnt(
		int expectedCnt,
		RunnableWithException<OperationFailedException> operation
	) throws OperationFailedException {
		// We rely on static variables in the class, so no concurrency allowed
		synchronized(FeatureCalculationFixture.MockCalculation.class) {
			
			int before = MockCalculation.cntExecutedCalled;
			operation.run();
			int diff = MockCalculation.cntExecutedCalled - before;
			
			assertEquals(expectedCnt, diff);
		}
	}
	
	/**
	 * A feature that returns the number-of-voxels in an object by using a {@link MockCalculation} internally
	 * 
	 * @author Owen Feehan
	 *
	 */
	public static class MockFeatureWithCalculation extends FeatureObjMask {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
			return input.calc( new MockCalculation() );
		}
	}
	
	/** Counts the number of pixels in the object, but uses a static variable to record the number of times executed is called */
	private static class MockCalculation extends FeatureCalculation<Double,FeatureInputSingleObj> {

		// Incremented every time executed is called
		public static int cntExecutedCalled = 0;
		
		@Override
		protected Double execute(FeatureInputSingleObj input) throws FeatureCalcException {
			cntExecutedCalled++;
			return (double) input.getObjMask().numPixels();
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof FeatureCalculationFixture.MockCalculation;
		}

		@Override
		public int hashCode() {
			return 7;
		}
	}
}