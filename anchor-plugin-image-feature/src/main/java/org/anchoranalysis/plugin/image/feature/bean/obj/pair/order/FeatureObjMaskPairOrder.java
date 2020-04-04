package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
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
		
	protected double valueFromObj1( CacheableParams<FeatureObjMaskPairParams> params ) throws FeatureCalcException {
		return featureValFrom(params.getParams().getObjMask1(), getCacheSession().additional(0), params );
	}
	
	protected double valueFromObj2( CacheableParams<FeatureObjMaskPairParams> params ) throws FeatureCalcException {
		return featureValFrom(params.getParams().getObjMask2(), getCacheSession().additional(1), params );
	}
	
	private double featureValFrom( ObjMask om, FeatureSessionCacheRetriever cache, CacheableParams<FeatureObjMaskPairParams> params ) throws FeatureCalcException {
		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams(om);
		paramsNew.setNrgStack( params.getParams().getNrgStack() );
		return cache.calc(
			feature,
			params.changeParams(paramsNew)
		);
	}
	
	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
}
