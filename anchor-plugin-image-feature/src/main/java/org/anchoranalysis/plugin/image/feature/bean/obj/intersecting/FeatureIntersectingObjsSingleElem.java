package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import java.util.ArrayList;
import java.util.List;


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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class FeatureIntersectingObjsSingleElem extends FeatureIntersectingObjs {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputPairObjs> item;
	// END BEAN PROPERTIES
	
	@Override
	protected double valueFor(SessionInput<FeatureInputSingleObj> params, RslvdCachedCalculation<ObjMaskCollection, FeatureInputSingleObj> intersecting)
			throws FeatureCalcException {

		try {
			return aggregateResults(
				calcResults(params, intersecting)
			);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	protected abstract double aggregateResults( List<Double> results );
	
	private List<Double> calcResults( SessionInput<FeatureInputSingleObj> paramsExst, RslvdCachedCalculation<ObjMaskCollection, FeatureInputSingleObj> ccIntersecting ) throws FeatureCalcException, ExecuteException {
		
		int size = ccIntersecting.getOrCalculate(paramsExst.getParams()).size();
		
		List<Double> results = new ArrayList<>();
		for( int i=0; i<size; i++) {
			
			final int index = i;
			
			double res = paramsExst.calcChangeParamsDirect(
				item,
				new CalculateIntersecting(ccIntersecting, index),
				"intersecting_obj_" + i
			);
			results.add(res);
		}
		return results;
	}
	
	public Feature<FeatureInputPairObjs> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputPairObjs> item) {
		this.item = item;
	}


}
