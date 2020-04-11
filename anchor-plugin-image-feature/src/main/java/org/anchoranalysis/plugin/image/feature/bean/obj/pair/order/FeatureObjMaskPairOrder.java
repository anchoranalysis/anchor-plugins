package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import java.util.function.Function;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
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
	private Feature<FeatureObjMaskParams> feature;
	// END BEAN PROPERTIES
		
	protected double valueFromObj1( CacheableParams<FeatureObjMaskPairParams> params ) throws FeatureCalcException {
		return featureValFrom(
			params,
			p -> p.getObjMask1(),
			"obj1"
		);
	}
	
	protected double valueFromObj2( CacheableParams<FeatureObjMaskPairParams> params ) throws FeatureCalcException {
		return featureValFrom(
			params,
			p -> p.getObjMask2(),
			"obj2"
		);
	}
	
	private double featureValFrom( CacheableParams<FeatureObjMaskPairParams> params, Function<FeatureObjMaskPairParams,ObjMask> extractObjFunc, String sessionName ) throws FeatureCalcException {
	
		return params.calcChangeParams(
			feature,
			p -> objMaskParams(p, extractObjFunc),
			sessionName
		);
	}
	
	private static FeatureObjMaskParams objMaskParams( FeatureObjMaskPairParams params, Function<FeatureObjMaskPairParams,ObjMask> extractObjFunc ) {
		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams(
			extractObjFunc.apply(params)
		);
		paramsNew.setNrgStack( params.getNrgStack() );
		return paramsNew;
	}
	
	public Feature<FeatureObjMaskParams> getFeature() {
		return feature;
	}

	public void setFeature(Feature<FeatureObjMaskParams> feature) {
		this.feature = feature;
	}
}
