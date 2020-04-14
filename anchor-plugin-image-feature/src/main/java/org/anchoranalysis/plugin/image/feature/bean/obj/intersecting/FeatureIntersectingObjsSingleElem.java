package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class FeatureIntersectingObjsSingleElem extends FeatureIntersectingObjs {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureObjMaskPairParams> item;
	// END BEAN PROPERTIES
	
	@Override
	protected double valueFor(CacheableParams<FeatureObjMaskParams> params, ObjMaskCollection intersecting)
			throws FeatureCalcException {

		ObjMask om = params.getParams().getObjMask();
		NRGStackWithParams nrgStack = params.getParams().getNrgStack();
		
		// Create parameters
		List<FeatureObjMaskPairParams> listParams = deriveListParams(intersecting, om, nrgStack);

		return aggregateResults(
			calcResults(params, listParams)
		);
	}
	
	protected abstract double aggregateResults( List<Double> results );
	
	private List<Double> calcResults( CacheableParams<FeatureObjMaskParams> paramsExst, List<FeatureObjMaskPairParams> paramsToCalc ) throws FeatureCalcException {
		
		List<Double> results = new ArrayList<>();
		for( int i=0; i<paramsToCalc.size(); i++) {
			
			final int index = i;
			
			double res = paramsExst.calcChangeParams(
				item,
				p -> paramsToCalc.get(index),
				"obj" + i
			);
			results.add(res);
		}
		return results;
	}
	
	private static List<FeatureObjMaskPairParams> deriveListParams( ObjMaskCollection intersecting, ObjMask om, NRGStackWithParams nrgStack) {
		return intersecting.asList().stream().map(
				omIntersects -> createParams(om, omIntersects, nrgStack)
		).collect( Collectors.toList() );
	}
	
	private static FeatureObjMaskPairParams createParams( ObjMask obj1, ObjMask obj2, NRGStackWithParams nrgStack) {
		return new FeatureObjMaskPairParams( obj1, obj2, nrgStack );
	}
	
	public Feature<FeatureObjMaskPairParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureObjMaskPairParams> item) {
		this.item = item;
	}


}