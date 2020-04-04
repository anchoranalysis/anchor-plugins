package ch.ethz.biol.cell.mpp.nrg.feature.objmask;



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
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cache.FeatureCacheDefinition;
import org.anchoranalysis.feature.cache.PrefixedCacheDefinition;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;

public abstract class DerivedObjMask extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double emptyValue = 255;
	
	@BeanField @SkipInit
	private Feature item;
	// END BEAN PROPERTIES
	
	private CachedCalculation<ObjMask> cc;
	
	private CachedCalculation<FeatureSessionCacheRetriever> ccSubsession;
	
	
	@Override
	public void beforeCalc(CacheableParams<FeatureInitParams> params)
			throws InitException {
		super.beforeCalc(params);
		
		cc = createCachedCalculation( params.getCacheSession() );
		
		ccSubsession = params.initThroughSubcacheSession(
			cacheDefinition().prefixForAdditionalCachesForChildren(),
			params.getParams(),
			item,
			getLogger()
		);
	}
	
	protected abstract CachedCalculation<ObjMask> createCachedCalculation( CacheSession session );
	

	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {

		try {
			ObjMask omDerived = cc.getOrCalculate(params.getParams());
			
			if (omDerived==null || !omDerived.hasPixelsGreaterThan(0)) {
				return emptyValue;
			}
			
			FeatureCalcParams paramsNew = createDerivedParams(omDerived,params.getParams());
			
			FeatureSessionCacheRetriever subCache = ccSubsession.getOrCalculate(null);
			
			// We select an appropriate cache for calculating the feature (should be the same as selected in init())
			return subCache.calc(
				item,
				params.changeParams(paramsNew)
			);
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}

	
	@Override
	public FeatureCalcParams transformParams(FeatureObjMaskParams params,Feature dependentFeature) throws FeatureCalcException {
		try {
			ObjMask omEroded = cc.getOrCalculate(params);
			
			if (omEroded==null || !omEroded.hasPixelsGreaterThan(0)) {
				return params;
			}
			
			return createDerivedParams(omEroded,params);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}			
	}
	
	
	public FeatureCalcParams createDerivedParams(ObjMask omDerived, FeatureObjMaskParams paramsExst) {

		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams( omDerived );
		paramsNew.setNrgStack( paramsExst.getNrgStack() );
		assert( paramsNew instanceof FeatureObjMaskParams);
		return paramsNew;
	}
	
	
	@Override
	protected FeatureCacheDefinition createCacheDefinition() {
		return new PrefixedCacheDefinition(
			this,
			prefixForAdditionalCachesForChildren()
		);
	}
	
	protected abstract String prefixForAdditionalCachesForChildren();
	
	public double getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

}
