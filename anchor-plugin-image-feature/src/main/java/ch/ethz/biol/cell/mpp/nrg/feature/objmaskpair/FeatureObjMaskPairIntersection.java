package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpair;

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
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculatePairIntersectionCommutative;

/**
 * TODO: CALCULATE HISTOGRAMS in the calculatePairIntersection rather than just object masks
 * 
 * This will save us some computation
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
	
	@BeanField @SkipInit
	private Feature item;
	// END BEAN PROPERTIES
	
	private CachedCalculation<FeatureSessionCacheRetriever> ccSubsession;
	
	private CachedCalculation<ObjMask> cc;
	
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		try {
			assert cache.hasBeenInit();
			
			cc = CalculatePairIntersectionCommutative.createFromCache(
				cache.main(),
				cache.additional(0),
				cache.additional(1),
				iterationsDilation,
				iterationsErosion,
				do3D
			);
			
			ccSubsession = cache.main().initThroughSubcacheSession(
				subCacheName(),
				params,
				item,
				getLogger()
			);
			
		} catch (CreateException e) {
			throw new InitException(e);
		}
	}
	

	
	@Override
	public double calcCast(FeatureObjMaskPairParams params) throws FeatureCalcException {
		
		try {
			ObjMask omIntersection = cc.getOrCalculate(params);
			
			if (omIntersection==null || !omIntersection.hasPixelsGreaterThan(0)) {
				return emptyValue;
			}
			
			FeatureCalcParams paramsNew = createParamsForIntersection(omIntersection,params);
			
			FeatureSessionCacheRetriever subCache = ccSubsession.getOrCalculate(null);
			assert( subCache.hasBeenInit() );
			
			// We select an appropriate cache for calculating the feature (should be the same as selected in init())
			return subCache.calc(item, paramsNew );
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}

	
	@Override
	public FeatureCalcParams transformParams(FeatureObjMaskPairParams params,
			Feature dependentFeature) throws FeatureCalcException {
		try {
			ObjMask omIntersection = cc.getOrCalculate(params);
			
			if (omIntersection==null || !omIntersection.hasPixelsGreaterThan(0)) {
				return params;
			}
			
			return createParamsForIntersection(omIntersection,params);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}			
	}
	
	
	public FeatureCalcParams createParamsForIntersection(ObjMask omIntersection, FeatureObjMaskPairParams paramsExst) {

		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams( omIntersection );
		paramsNew.setNrgStack( paramsExst.getNrgStack() );
		assert( paramsNew instanceof FeatureObjMaskParams);
		return paramsNew;
	}
	
	private String subCacheName() {
		return "intersection_" + iterationsDilation + "_" + iterationsErosion + "_" + do3D;
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

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

}
