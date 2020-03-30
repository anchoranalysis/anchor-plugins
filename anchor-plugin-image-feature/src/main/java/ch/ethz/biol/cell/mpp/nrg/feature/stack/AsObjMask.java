package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cache.ComplexCacheDefinition;
import org.anchoranalysis.feature.cache.FeatureCacheDefinition;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.feature.session.cache.NullCacheRetriever;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Treats a channel as an object-mask, assuming binary valus of 0 and 255
 * and calls an object-mask feature
 * 
 * @author FEEHANO
 *
 */
public class AsObjMask extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private Feature item;
	
	@BeanField
	/** The channel that that forms the binary mask */
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	private FeatureSessionCacheRetriever subcache;

	@Override
	public FeatureCacheDefinition cacheDefinition() {
		return new ComplexCacheDefinition(this, new String[]{"additionalCache"} );
	}
	
	
	/**
	 *  Special initialisation with different params for 'item' as it is elsewhere ignored in the initialisation
	 */
	@Override
	public void beforeCalc(FeatureInitParams params,
			CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);

		
		// TODO fix
		//  Work around. Creates a new cache 
		this.subcache = new NullCacheRetriever( cache.main().getSharedFeatureList() );
		
		cache.initThroughSubcache(subcache, params, item, getLogger() );
	}
	
	@Override
	public double calcCast(FeatureStackParams params) throws FeatureCalcException {
				
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		try {
			ObjMask om = extractObjMask(params);
			paramsObj.setNrgStack( params.getNrgStack() );
			paramsObj.setObjMask( om );
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
		
		return subcache.calc(item, paramsObj);
	}
		
	private ObjMask extractObjMask(FeatureStackParams params) throws CreateException {
		Chnl chnl = params.getNrgStack().getChnl(nrgIndex);
		BinaryChnl binary = new BinaryChnl(chnl, BinaryValues.getDefault());
		
		return new ObjMask( binary.binaryVoxelBox() );
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}
}
