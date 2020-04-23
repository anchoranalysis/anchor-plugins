package org.anchoranalysis.plugin.image.feature.bean.obj.pair;

import java.util.Optional;


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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsForIntersection;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsFromDelegateOption;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculatePairIntersectionCommutative;

/**
 * Finds the intersection of two objects and calculates a feature on it
 * 
 * <p>TODO: CALCULATE HISTOGRAMS in the calculatePairIntersection rather than just object masks. This will save us some computation</p>
 * <p>TODO: test properly due to weird use of cache</p>
 * 
 *  
 * @author Owen Feehan
 *
 */
public class FeatureObjMaskPairIntersection extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @Positive
	private int iterationsDilation = 0;
	
	@BeanField
	private int iterationsErosion = 0;
		
	@BeanField
	private boolean do3D = true;
	
	@BeanField
	private double emptyValue = 255;
	
	@BeanField
	private Feature<FeatureInputSingleObj> item;
	// END BEAN PROPERTIES
	
	private static final String CACHE_INTERSECTION = "intersection_eroded";
	private static final String CACHE_OBJ1 = "obj1";
	private static final String CACHE_OBJ2 = "obj2";
		
	@Override
	public double calc(CacheableParams<FeatureInputPairObjs> params) throws FeatureCalcException {
		return CalculateParamsFromDelegateOption.calc(
			params,
			createCCIntersection(params),
			delegate -> new CalculateParamsForIntersection(delegate),
			item,
			cacheIntersectionName(),
			emptyValue
		);
	}
	
	@Override
	public CacheableParams<FeatureInput> transformParams(CacheableParams<FeatureInputPairObjs> params,
			Feature<FeatureInput> dependentFeature) throws FeatureCalcException {
		
		//Optional<FeatureObjMaskParams> paramsDerived = createParamsForIntersection(params);
		// TODO FIX
		/*CacheableParams<FeatureCalcParams> p = params 
				.map( p->
			p.upcastParams()
		);*/
		return null;
	}
	
	/** A unique cache-name for the parameterization of how we find a parameterization */
	private String cacheIntersectionName() {
		return String.format(
			"%s_%d_%d_%d",
			CACHE_INTERSECTION,
			iterationsDilation,
			iterationsErosion,
			do3D ? 1 : 0
		);
	}

	private CachedCalculation<Optional<ObjMask>,FeatureInputPairObjs> createCCIntersection(CacheableParams<FeatureInputPairObjs> params) throws FeatureCalcException {
		try {
			return CalculatePairIntersectionCommutative.createFromCache(
				params,
				params.cacheFor(CACHE_OBJ1, FeatureInputPairObjs.class),
				params.cacheFor(CACHE_OBJ2, FeatureInputPairObjs.class),
				iterationsDilation,
				iterationsErosion,
				do3D
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}

	public int getIterationsDilation() {
		return iterationsDilation;
	}

	public void setIterationsDilation(int iterationsDilation) {
		this.iterationsDilation = iterationsDilation;
	}

	public double getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}

	public int getIterationsErosion() {
		return iterationsErosion;
	}

	public void setIterationsErosion(int iterationsErosion) {
		this.iterationsErosion = iterationsErosion;
	}

	public Feature<FeatureInputSingleObj> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleObj> item) {
		this.item = item;
	}

}
