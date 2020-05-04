package org.anchoranalysis.test.feature.plugins;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;

public class MockFeatureWithCalculationFixture {

	@FunctionalInterface
	public interface RunnableWithException<E extends Throwable> {
	    public void run() throws E;
	}
	
	/**
	 * Executes an operation, and afterwards asserts an expected number of times the {@link MockCalculation.execute()} method was called.
	 * 
	 * @param expectedCntExecute the expected number of calls to {@link MockCalculation.execute()}
	 * @param operation an operation, typically involving {@link MockCalculation}
	 * @throws OperationFailedException
	 */
	public static void executeAndAssertCnt(
		int expectedCntCalc,
		int expectedCntExecute,
		RunnableWithException<OperationFailedException> operation
	) throws OperationFailedException {
		// We rely on static variables in the class, so no concurrency allowed
		synchronized(MockFeatureWithCalculationFixture.MockCalculation.class) {
			
			int beforeExecute = MockCalculation.cntExecuteCalled;
			int beforeCalc = MockFeatureWithCalculation.cntCalcCalled;
			
			operation.run();
			
			assertEquals(
				expectedCntExecute,
				MockCalculation.cntExecuteCalled - beforeExecute
			);
			
			assertEquals(
				expectedCntCalc,
				MockFeatureWithCalculation.cntCalcCalled - beforeCalc
			);
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

		
		// Incremented every calc executed is called
		public static int cntCalcCalled = 0;
		
		@Override
		protected double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
			cntCalcCalled++;
			return input.calc( new MockCalculation() );
		}
	}
	
	/** Counts the number of pixels in the object, but uses a static variable to record the number of times executed is called */
	private static class MockCalculation extends FeatureCalculation<Double,FeatureInputSingleObj> {

		// Incremented every time executed is called
		public static int cntExecuteCalled = 0;
		
		/** Can be changed from the default implementation as desired */
		public static Function<FeatureInputSingleObj,Double> funcCalculation = input -> (double) input.getObjMask().numPixels();
		
		@Override
		protected Double execute(FeatureInputSingleObj input) throws FeatureCalcException {
			cntExecuteCalled++;
			return funcCalculation.apply(input);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof MockFeatureWithCalculationFixture.MockCalculation;
		}

		@Override
		public int hashCode() {
			return 7;
		}
	}
}
