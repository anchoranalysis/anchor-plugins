package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Base class for evaluating FeatureObjMaskPair in terms of the order of the elements (first object, second order etc.)
 * 
 * @author owen
 *
 */
public abstract class FeatureObjMaskPairOrder extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature feature;
	// END BEAN PROPERTIES
		
	protected double featureValFrom( ObjMask om ) throws FeatureCalcException {
		FeatureObjMaskParams params = new FeatureObjMaskParams(om);
		params.setNrgStack( params.getNrgStack() );
		return getCacheSession().calc( feature, params );
	}
	
	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
}
