package org.anchoranalysis.bean.provider.objs.merge;

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


import java.util.HashMap;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.cache.FeatureSessionCache;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.objmask.ObjMask;

class MergeSession extends FeatureSession {

	private SequentialSession delegate;
	
	private FeatureInitParams paramsInit;
	
	private HashMap<ObjMask,FeatureSessionCache> map;
	
	private Feature feature;
	
	// Only caches calculation results which are positive
	private static boolean onlyCacheNonNegativeResults = true;
	
	private LogErrorReporter logger;
		
	public MergeSession( Feature feature, FeatureInitParams initParams, boolean recordTimes ) {
		
		// We duplicate the feature, so it can be inited() again with a new seperate cache
		this.feature = feature.duplicateBean();
		delegate = new SequentialSession( this.feature );
		this.paramsInit = initParams.duplicate();
	
		map = new HashMap<ObjMask,FeatureSessionCache>();
	}
	
	public void start( SharedFeatureSet sharedFeatures, LogErrorReporter logger ) throws InitException {
		this.logger = logger;
		delegate.start( paramsInit, sharedFeatures, logger );
	}
	
	public void clearCache() {
		map.clear();
	}
	
	
	private FeatureCalcParams createParams( ObjMask obj1, ObjMask obj2, ObjMask omMerged ) throws CreateException {
		FeatureObjMaskPairMergedParams params = new FeatureObjMaskPairMergedParams(obj1, obj2,omMerged);
		params.setNrgStack( new NRGStackWithParams(paramsInit.getNrgStack(),paramsInit.getKeyValueParams()) );
		return params;
	}
	
	private void putInMap( String key, ObjMask om ) throws InitException {
		FeatureSessionCache src = delegate.getCache().getAdditionalCache( key );
		try {
			FeatureSessionCache cacheNew = src.duplicate();
			cacheNew.init(paramsInit, logger, false);
			
			cacheNew.assignResult( src );
			map.put(om, cacheNew);
		} catch (CreateException | OperationFailedException e) {
			throw new InitException(e);
		}
	}

	// We calculate the features without invalidating the cache, instead replacing the existing CachedCacheLists with
	//  ones we remembered from previous calculations
	public double calc( ObjMask obj1, ObjMask obj2, ObjMask omMerged )
			throws FeatureCalcException {
		try {
			FeatureSessionCache cache0 = map.get(obj1);
			FeatureSessionCache cache1 = map.get(obj2);
			FeatureSessionCache cache2 = map.get(omMerged);
			
			delegate.getCache().invalidate();
			
			// We see if an object-mask already exists in the cache, and if so, we load the cached-calculations from there
			if (cache0!=null) {
				FeatureSessionCache cacheAdd = delegate.getCache().getAdditionalCache("first");
				if (cacheAdd!=null) {
					cacheAdd.assignResult( cache0 );
				}
			}
			if (cache1!=null) {
				FeatureSessionCache cacheAdd = delegate.getCache().getAdditionalCache("second");
				if (cacheAdd!=null) {
					cacheAdd.assignResult( cache1 );
				}
			}
			if (cache2!=null) {
				FeatureSessionCache cacheAdd = delegate.getCache().getAdditionalCache("merged");
				if (cacheAdd!=null) {
					cacheAdd.assignResult( cache2 );
				}
			}
			
			
			
			
			// We re-initialize the feature to have our new cached results
			//feature.initRecursive( this.paramsInit, delegate.getCache().retriever());
			FeatureCalcParams params = createParams(obj1,obj2,omMerged);
			double val = delegate.getCache().retriever().calc(feature, params );
						
			//System.out.printf("Calculating (%s,%s,%s) has sizes (%d,%d,%d) = %f\n", obj1.centerOfGravity(), obj2.centerOfGravity(), omMerged.centerOfGravity(), delegate.getCache().getAdditionalCache(0).currentSize(), delegate.getCache().getAdditionalCache(1).currentSize(), delegate.getCache().getAdditionalCache(2).currentSize(), val );
			
			// We always cache the single-boejct
			if (cache0==null) {
				putInMap( "first", obj1 );
			}
			
			if (cache1==null) {
				putInMap( "second", obj2 );
			}
			
			// For the merged-object, we only bother or if we're positive (or onlyCacheNonNegativeResults is false)
			if (!onlyCacheNonNegativeResults || val>=0) {
				// We see if an object-mask already exists in cache, and if not, we save the cached calculations there

				if (cache2==null) {
					putInMap( "merged", omMerged );
				}
			}
			
			return val;
		} catch (CreateException | OperationFailedException | InitException e) {
			throw new FeatureCalcException(e);
		}
	}
	
}
