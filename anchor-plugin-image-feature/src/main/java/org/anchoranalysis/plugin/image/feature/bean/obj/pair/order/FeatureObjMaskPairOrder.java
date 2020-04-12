package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
