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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPairMerged;
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
	@BeanField
	private Feature<FeatureObjMaskParams> item;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureObjMaskPairMergedParams> params)
			throws FeatureCalcException {
		
		return transformParamsCast(params).calc(item);
	}
	
	public CacheableParams<FeatureObjMaskParams> transformParamsCast( CacheableParams<FeatureObjMaskPairMergedParams> params ) {

		assert( params.getParams() instanceof FeatureObjMaskPairMergedParams );
						
		return params.mapParams(
			p -> extractObj(p),
			cacheNameToUse()
		);
	}
	
	protected abstract ObjMask selectObjMask( FeatureObjMaskPairMergedParams params );
	
	/** The name of the cache to use for calculation */
	protected abstract String cacheNameToUse();
	
	public Feature<FeatureObjMaskParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureObjMaskParams> item) {
		this.item = item;
	}
	
	private FeatureObjMaskParams extractObj( FeatureObjMaskPairMergedParams params ) {
		
		ObjMask omSelected = selectObjMask(params);
		
		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams( omSelected );
		paramsNew.setNrgStack( params.getNrgStack() );
		assert( paramsNew instanceof FeatureObjMaskParams);
		return paramsNew;
	}
}
