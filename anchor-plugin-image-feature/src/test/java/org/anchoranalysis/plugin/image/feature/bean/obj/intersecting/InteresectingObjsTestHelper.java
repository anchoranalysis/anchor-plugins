package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.test.LoggingFixtures;
import org.anchoranalysis.test.feature.plugins.CircleObjMaskFixture;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculator;
import org.mockito.Mockito;

class InteresectingObjsTestHelper {

	
	private static final String ID = "someObjs";
	
	private static final int numInteresecting = 4;
	private static final int numNotIteresecting = 2;
	
	/**
	 * Runs several tests on a feature and object-mask collection by removing objects at particular indexes
	 * 
	 * <p>Specifically the test that is called is the same as {#link assertFeatureIndexInt}</p>
	 * 
	 * @param feature feature to use for test
	 * @param expectedFirst expected-result for object in first-position
	 * @param expectedSecond expected-result for object in second-position
	 * @param expectedSecondLast expected-result for object in second-last position
	 * @param expectedLast expected-result for object in last position
	 * @throws OperationFailedException
	 * @throws FeatureCalcException
	 * @throws InitException
	 */
	public static void testPositions(
		String messagePrefix,			
		FeatureIntersectingObjs feature,
		boolean sameSize,
		int expectedFirst,
		int expectedSecond,
		int expectedSecondLast,
		int expectedLast
	) throws OperationFailedException, FeatureCalcException, InitException {
		
		ObjMaskCollection objs = IntersectingObjsFixture.generateIntersectingObjs(
			numInteresecting,
			numNotIteresecting,
			sameSize
		);
		
		// First object
		InteresectingObjsTestHelper.assertFeatureIndexInt(
			combine(messagePrefix, "first"),
			feature,
			objs,
			0,
			expectedFirst
		);
				
		// Second object
		InteresectingObjsTestHelper.assertFeatureIndexInt(
			combine(messagePrefix, "second"),
			feature,
			objs,
			1,
			expectedSecond
		);
		
		// Second last object
		int secondLastIndex = (numInteresecting+numNotIteresecting)-2;
		InteresectingObjsTestHelper.assertFeatureIndexInt(
			combine(messagePrefix, "second-last"),
			feature,
			objs,
			secondLastIndex,
			expectedSecondLast
		);
		
		// Last object
		int lastIndex = (numInteresecting+numNotIteresecting)-1;
		InteresectingObjsTestHelper.assertFeatureIndexInt(
			combine(messagePrefix, "last"),
			feature,
			objs,
			lastIndex,
			expectedLast
		);		
	}
	
	/** 
	 * Asserts a result after extracting object at index i from a collection, and using the remainder as the object-collection
	 * 
	 * @param descriptive-message for test
	 * @param feature feature to calculate on params to form value
	 * @param objs object-collection used to determine parameter for feature (single object removed at index) and the remainder that form a set of objects to intersect with
	 * @param index index of object in collection to remove and use as parameter
	 * @param expectedResult expected result from test 
	 *  
	 * @throws InitException 
	 * @throws FeatureCalcException 
	 * @throws OperationFailedException
	 **/
	private static void assertFeatureIndexInt(
		String message,
		FeatureIntersectingObjs feature,
		ObjMaskCollection objs,
		int index,
		int expectedResult
	) throws OperationFailedException, FeatureCalcException, InitException {
		
		// We take the second object in the collection, as one that should intersect with 2 others
		ObjMask om = objs.get(index);
		ObjMaskCollection others = removeImmutable(objs, index);
				
		// We take the final objection the collection , as one
		
		FeatureTestCalculator.assertIntResult(
			message,
			addId(feature),
			new FeatureObjMaskParams(om, CircleObjMaskFixture.nrgStack()),
			Optional.of(createInitParams(others)),
			expectedResult
		);		
	}
	
	/** Removes an object from the collection immutably */
	private static ObjMaskCollection removeImmutable( ObjMaskCollection objs, int index ) {
		ObjMaskCollection out = objs.duplicateShallow();
		out.asList().remove(index);
		return out;
	}
	
	private static ImageInitParams createInitParams( ObjMaskCollection others ) throws OperationFailedException {
		
		SharedObjects so = new SharedObjects(
			LoggingFixtures.simpleLogErrorReporter()
		);
		
		so.getOrCreate(ObjMaskCollection.class).add(ID, ()->others);
		
		return ImageInitParams.create(
			so,
			Mockito.mock(Path.class)
		);
	}
	
	private static FeatureIntersectingObjs addId( FeatureIntersectingObjs feature ) {
		feature.setId(ID);
		return feature;
	}
	
	private static String combine(String first, String second) {
		return first + "-" + second;
	}
}
