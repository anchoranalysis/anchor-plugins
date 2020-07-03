package org.anchoranalysis.plugin.image.feature.object.calculation.single;

/*-
 * #%L
 * anchor-plugin-image-feature
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateIncrementalOperationMap;
import org.anchoranalysis.test.image.NRGStackFixture;
import org.anchoranalysis.test.image.obj.ObjMaskFixture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CalculateIncrementalOperationMapTest {

	@Spy
	private CalculateIncrementalOperationMap mockMap = spy(CalculateIncrementalOperationMapFixture.class);

	private NRGStackWithParams nrgStack = NRGStackFixture.create(true, true); 
	
	private FeatureInputSingleObject input = new FeatureInputSingleObject(
		mock(ObjectMask.class),
		nrgStack
	);
	
	@Before
    public void setup() throws OperationFailedException {
		// An arbitrary object
		ObjectMask om = new ObjMaskFixture(nrgStack.getDimensions()).create1();

		when( mockMap.applyOperation(any(), any(), anyBoolean()) ).thenReturn( om );
    }
	   
	@Test
	public void testInsertingAndAppending() throws OperationFailedException, FeatureCalcException {
				
		int NUM_FIRST = 8;
		int NUM_ADDITIONAL = 3;
		int NUM_LESS_THAN_FIRST = NUM_FIRST - 2;
		
		// Seek a number of objects, where nothing already exists in the cache
		callMockAndVerify(input, NUM_FIRST, 1, NUM_FIRST);
		
		// Seek an additional number more, given that NUM_FIRST already exists
		callMockAndVerify(input, NUM_FIRST+NUM_ADDITIONAL, 2, NUM_FIRST+NUM_ADDITIONAL);
		
		// Seek a number we already know is in the cache
		callMockAndVerify(input, NUM_LESS_THAN_FIRST, 2, NUM_FIRST+NUM_ADDITIONAL);
	}
	
	@Test
	public void testInvalidate() throws FeatureCalcException {
		// Grow to an initial number
		int NUM_INITIAL = 6;
		mockMap.getOrCalculate(input, NUM_INITIAL);
		assertStoredCount(NUM_INITIAL);

		// Invalidate and check that all objects are gone
		mockMap.invalidate();
		assertStoredCount(0);
	}
	
	private void assertStoredCount( int expectedNumItemsStored ) {
		assertEquals(expectedNumItemsStored, mockMap.numItemsCurrentlyStored());
	}
	
	private void callMockAndVerify(FeatureInputSingleObject input, int key, int executeTimes, int expectedNumItemsInCache) throws FeatureCalcException, OperationFailedException {
		mockMap.getOrCalculate(input, key);
		verifyMethodCalls(executeTimes, expectedNumItemsInCache);
		assertStoredCount(expectedNumItemsInCache);
	}
	
	private void verifyMethodCalls(int executeTimes, int applyOperationTimes) throws FeatureCalcException, OperationFailedException {
		verify( mockMap, times(executeTimes) ).execute(
			any(),
			any()
		);
		verify( mockMap, times(applyOperationTimes) ).applyOperation(
			any(),
			any(),
			anyBoolean()
		);
	}
	
	/** Constructor with zero parameters for CalculateIncrementalOperationMap to facilitate mocking */
	private static abstract class CalculateIncrementalOperationMapFixture extends CalculateIncrementalOperationMap {

		public CalculateIncrementalOperationMapFixture() {
			super(true);
		}
	}
}