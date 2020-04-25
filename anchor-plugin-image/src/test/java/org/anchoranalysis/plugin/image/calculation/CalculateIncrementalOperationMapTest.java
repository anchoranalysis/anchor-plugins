package org.anchoranalysis.plugin.image.calculation;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
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
	
	private FeatureInputSingleObj input = new FeatureInputSingleObj(
		mock(ObjMask.class),
		nrgStack
	);
	
	@Before
    public void setup() throws OperationFailedException {
		// An arbitrary object
		ObjMask om = new ObjMaskFixture(nrgStack.getDimensions()).create1();

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
	
	private void callMockAndVerify(FeatureInputSingleObj input, int key, int executeTimes, int expectedNumItemsInCache) throws FeatureCalcException, OperationFailedException {
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
