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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsForIntersection;

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
	private Feature<FeatureObjMaskParams> item;
	// END BEAN PROPERTIES
	
	private static final String CACHE_INTERSECTION = "intersection_eroded"; // TODO add iterationsErosion to name
	private static final String CACHE_OBJ1 = "obj1";	// TODO  add iterationsDilation to name
	private static final String CACHE_OBJ2 = "obj2";
		
	@Override
	public double calc(CacheableParams<FeatureObjMaskPairParams> params) throws FeatureCalcException {
		
		CachedCalculation<Optional<FeatureObjMaskParams>,FeatureObjMaskPairParams> ccParamsDerived
			= createParamsForIntersection(params);
		
		try {
			Optional<FeatureObjMaskParams> paramsDerived = params.calc(ccParamsDerived);
			
			if (!paramsDerived.isPresent()) {
				return emptyValue;
			}
			
			// We select an appropriate cache for calculating the feature (should be the same as selected in init())
			return params.calcChangeParams(
				item,
				p -> paramsDerived.get(),
				CACHE_INTERSECTION
			);
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}
	
	@Override
	public CacheableParams<FeatureCalcParams> transformParams(CacheableParams<FeatureObjMaskPairParams> params,
			Feature<FeatureCalcParams> dependentFeature) throws FeatureCalcException {
		
		//Optional<FeatureObjMaskParams> paramsDerived = createParamsForIntersection(params);
		// TODO FIX
		/*CacheableParams<FeatureCalcParams> p = params 
				.map( p->
			p.upcastParams()
		);*/
		return null;
	}
		
	public CachedCalculation<Optional<FeatureObjMaskParams>,FeatureObjMaskPairParams> createParamsForIntersection(
		CacheableParams<FeatureObjMaskPairParams> paramsExst
	) throws FeatureCalcException {
		
		try {
			return CalculateParamsForIntersection.createFromCache(
				paramsExst,
				createCCIntersection(paramsExst)
			);
		
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	private CachedCalculation<Optional<ObjMask>,FeatureObjMaskPairParams> createCCIntersection(CacheableParams<FeatureObjMaskPairParams> params) throws CreateException {
			return CalculatePairIntersectionCommutative.createFromCache(
				params,
				params.cacheFor(CACHE_OBJ1, FeatureObjMaskPairParams.class),
				params.cacheFor(CACHE_OBJ2, FeatureObjMaskPairParams.class),
				iterationsDilation,
				iterationsErosion,
				do3D
			);
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

	public Feature<FeatureObjMaskParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureObjMaskParams> item) {
		this.item = item;
	}

}
