package org.anchoranalysis.bean.provider.objs.merge;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.bean.merge.ObjMaskProviderMergeBase;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.objs.IntersectingCircleObjsFixture;

class MergeTestHelper {

	private static final int NUM_LINEAR_INTERSECTING = 8;
	private static final int NUM_LINEAR_NOT_INTERSECTING = 3;
	
	/** Everything that intersects merged */
	public static final int EXPECTED_LINEAR_RESULT_ALL_INTERSECTING_MERGED = NUM_LINEAR_NOT_INTERSECTING + 1;
	public static final int EXPECTED_LINEAR_RESULT_FIRST_THREE_NOT_MERGING = NUM_LINEAR_NOT_INTERSECTING + 3 + 1;
	
	/** Linear intersection (intersects with left and right neighbour) among the first 8 objects, and then 3 more than don't intersect
	 * 
	 * <pre>i.e. a pattern     a--b--c--d--e--f--g--h i j k   where  --  represents a neighbour hood relation</pre>
	 * 
	 * <pre>The sizes of the 11 objects increase i.e. 81, 149, 253, 377, 529, ....., 1653, 1961
	 */
	public static final ObjMaskCollection OBJS_LINEAR_INTERSECTING = IntersectingCircleObjsFixture.generateIntersectingObjs(
		NUM_LINEAR_INTERSECTING,
		NUM_LINEAR_NOT_INTERSECTING,
		false
	);
	
	private MergeTestHelper() {}
	
	public static void testProviderOn( int expectedFinalCount, ObjMaskProviderMergeBase provider ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		ProviderFixture.initProvider(provider, logger);
	
		ObjMaskCollection mergedObjs = provider.create();
		assertEquals(expectedFinalCount, mergedObjs.size());
	}
}
