package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.bean.object.single.NumberVoxels;

class OrderHelper {

	private OrderHelper() {
		
	}
		
	public static FeatureDeriveFromPair addFeatureToOrder( FeatureDeriveFromPair feature ) {
		// Need an object-mask feature
		feature.setItem( new NumberVoxels() );
		return feature;
	}
}
