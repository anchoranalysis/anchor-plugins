package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.image.feature.bean.objmask.NumVoxels;

class OrderHelper {

	private OrderHelper() {
		
	}
		
	public static Feature addFeatureToOrder( FeatureObjMaskPairOrder feature ) {
		// Need an object-mask feature
		feature.setFeature( new NumVoxels() );
		return feature;
	}
}
