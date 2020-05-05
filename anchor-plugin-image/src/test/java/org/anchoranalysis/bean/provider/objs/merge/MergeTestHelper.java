package org.anchoranalysis.bean.provider.objs.merge;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.bean.merge.ObjMaskProviderMergeBase;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.anchoranalysis.test.feature.plugins.objs.IntersectingCircleObjsFixture;

class MergeTestHelper {

	private static final int NUM_INTERSECTING = 8;
	private static final int NUM_NOT_INTERSECTING = 3;
	
	/** Everything that intersects merged */
	public static final int EXPECTED_RESULT_ALL_INTERSECTING_MERGED = NUM_NOT_INTERSECTING + 1;
	public static final int EXPECTED_RESULT_FIRST_THREE_NOT_MERGING = NUM_NOT_INTERSECTING + 3 + 1;
	
	
	/** Linear intersection (intersects with left and right neighbor) among the first 8 objects, and then 3 more than don't intersect
	 * 
	 * <pre>i.e. a pattern     a--b--c--d--e--f--g--h i j k   where  --  represents a neighbour hood relation</pre>
	 * 
	 * <pre>The sizes of the 11 objects increase i.e. 81, 149, 253, 377, 529, ....., 1653, 1961
	 */
	public static final ObjMaskCollection OBJS_LINEAR_INTERSECTING = IntersectingCircleObjsFixture.generateIntersectingObjs(
		NUM_INTERSECTING,
		NUM_NOT_INTERSECTING,
		false
	);
	
	private MergeTestHelper() {}
	
	/**
	 * Tests the initialization and execution of a provider of object-masks that results in a number of merged-objects
	 * 
	 * @param expectedFinalMergeCount the expected size() of the object-masks returned by the provider
	 * @param expectedFeatureCalcCount the expected number of times the calc() method is called on the Feature
	 * @param expectedCalculationCount the expected number of times the execute() method is called on the Calculation
	 * @param provider a provider of object-masks
	 * 
	 * @throws OperationFailedException
	 */
	public static void testProviderOn(
		int expectedFinalMergeCount,
		int expectedFeatureCalcCount,
		int expectedCalculationCount,
		ObjMaskProviderMergeBase provider
	) throws OperationFailedException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		try {
			ProviderFixture.initProvider(provider, logger);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	
		MockFeatureWithCalculationFixture.executeAndAssertCnt(
			expectedFeatureCalcCount,
			expectedCalculationCount,
			() -> {
				try {
					ObjMaskCollection mergedObjs = provider.create();
					assertEquals(
						"final number of merged-objects",
						expectedFinalMergeCount,
						mergedObjs.size()
					);
				} catch (CreateException e) {
					throw new OperationFailedException(e);
				}
			}
		);
	}
}