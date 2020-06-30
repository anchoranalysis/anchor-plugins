package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.single.NumberVoxels;
import org.anchoranalysis.plugin.image.feature.bean.object.collection.intersecting.FeatureIntersectingObjs;
import org.anchoranalysis.plugin.image.feature.bean.object.collection.intersecting.FeatureIntersectingObjsSingleElem;
import org.anchoranalysis.plugin.image.feature.bean.object.collection.intersecting.FeatureIntersectingObjsThreshold;

class FeatureHelper {

	static final int VALUE_NO_OBJECTS = -1;
		
	static final int EXPECTED_NUM_PIXELS_FIRST_CIRCLE = 81;
	
	static final int EXPECTED_NUM_PIXELS_SECOND_CIRCLE = 149;
	
	static final int EXPECTED_NUM_PIXELS_SECOND_LAST_CIRCLE = 529;
	
	/** 
	 * The threshold placed on the number of voxels rejects the smaller (initial) circles before passing the later larger ones
	 * 
	 *  <p>Therefore the counts returned in these tests are initially 0 and latterly the same as {#link {@link NumIntersectingObjsTest}}
	 * 
	 * @return
	 */
	public static FeatureIntersectingObjs createWithThreshold( FeatureIntersectingObjsThreshold feature ) {
		feature.setThreshold( 90 );
		return createWithFeature(feature);
	}
	
	/**
	 * A pair feature (the number-of-voxels of the first object) is set on whatever feature is passed
	 * 
	 * @param feature feature to set new pair featore on as property
	 * @return feature with the proprty changed
	 */
	public static FeatureIntersectingObjs createWithFeature( FeatureIntersectingObjsSingleElem feature ) {
		feature.setItem( createPairFeature() );
		feature.setValueNoObjects(VALUE_NO_OBJECTS);
		return feature;
	}
	
	private static FeaturePairObjects createPairFeature() {
		return new First(
			new NumberVoxels()
		);
	}
}
