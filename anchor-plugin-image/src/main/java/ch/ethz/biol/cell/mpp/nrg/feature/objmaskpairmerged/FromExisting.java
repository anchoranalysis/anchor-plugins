package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cache.FeatureCacheDefinition;
import org.anchoranalysis.feature.cache.PrefixedCacheDefinition;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.objmask.pair.merged.FeatureObjMaskPairMerged;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Simply used ones of the the obj-masks, and calls a feature
 * 
 * @author Owen Feehan
 *
 */
public abstract class FromExisting extends FeatureObjMaskPairMerged {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private Feature item;
	// END BEAN PROPERTIES
	
	protected abstract ObjMask selectObjMask( FeatureObjMaskPairMergedParams params );
	
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskPairMergedParams> params)
			throws FeatureCalcException {

		FeatureCalcParams paramsNew = transformParams(params.getParams());
		
		// We select an appropriate cache for calculating the feature (should be the same as selected in init())
		return getCacheSession().additional(0).calc(
			item,
			params.changeParams(paramsNew)
		);
	}


	public FeatureCalcParams transformParams(FeatureObjMaskPairMergedParams params) {

		assert( params instanceof FeatureObjMaskPairMergedParams);
		
		ObjMask omSelected = selectObjMask(params);
		
		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams( omSelected );
		paramsNew.setNrgStack( params.getNrgStack() );
		
		assert( paramsNew instanceof FeatureObjMaskParams);
		
		return paramsNew;
	}
	
	
	@Override
	public FeatureCalcParams transformParams(FeatureCalcParams params,
			Feature dependentFeature) {

		if (params instanceof FeatureObjMaskPairMergedParams) {
			return transformParams( (FeatureObjMaskPairMergedParams) params );
		} else {
			return params;
		}
	}

	
	/**
	 *  Special initialisation with different params for 'item' as it is elsewhere ignored in the initialisation
	 */
	@Override
	public void beforeCalc(CacheableParams<FeatureInitParams> params)
			throws InitException {
		super.beforeCalc(params);
		
		FeatureSessionCacheRetriever subcache = params.getCacheSession().additional(0);

		params.initThroughSubcache(subcache, params.getParams(), item, getLogger() );
	}
	
	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	@Override
	protected FeatureCacheDefinition createCacheDefinition() {
		return new PrefixedCacheDefinition(
			this,
			prefixForAdditionalCachesForChildren()
		);
	}
	
	protected abstract String prefixForAdditionalCachesForChildren();

}
