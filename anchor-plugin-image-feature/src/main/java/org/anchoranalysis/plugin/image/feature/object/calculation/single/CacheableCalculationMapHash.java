package org.anchoranalysis.plugin.image.feature.object.calculation.single;

/*
 * #%L
 * anchor-feature
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
import java.util.Map;

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculationMap;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Implementation of a CachedCalculationMap using a Hash-Map
 * 
 * @author Owen Feehan
 *
 * @param <E> an exception thrown if something goes wrong during the calculation
 */
public abstract class CacheableCalculationMapHash<
	S,
	T extends FeatureInput,
	U,
	E extends Exception
> extends CacheableCalculationMap<S,T,U,E> {

	/**
	 * Caches our results for different Keys
	 */
	private transient Map<U,S> cache;
	
	
	/**
	 * Creates the class
	 * @param cacheSize cache-size to use for the keys
	 */
	public CacheableCalculationMapHash( int cacheSize ) {
		cache = new HashMap<U,S>();
	}

	/**
	 * Executes the operation and returns a result, either by doing the calculation, or retrieving
	 *   a cached-result from previously.
	 * 
	 * @param If there is no cached-value, and the calculation occurs, these parameters are used. Otherwise ignored.
	 * @return the result of the calculation
	 * @throws ExecuteException if the calculation cannot finish, for whatever reason
	 */
	@Override
	public S getOrCalculate( T input, U key ) throws E {
		
		S obj = cache.get(key);
		if (obj==null) {
			obj = execute( input, key );
			put( key, obj );
		}
		return obj;
	}
	
	/** Number of items currently stored in cache */
	public int numItemsCurrentlyStored() {
		return cache.size();
	}
	
	/**
	 * Invalidates the cache, removing any items already stored.
	 */
	@Override
	public void invalidate() {
		cache.clear();	

	}
	
	/**
	 *  Gets an existing result for the current params from the cache.
	 *  
	 * @param key
	 * @return a cached-result, or NULL if it doesn't exist
	 * @throws FeatureCalcException 
	 * @throws GetOperationFailedException 
	 */
	protected S getOrNull( U key ) throws E {
		return cache.get(key);
	}
	
	protected boolean hasKey( U key ) {
		return cache.get(key)!=null;
	}
	
	protected void put(U index, S item) {
		cache.put(index, item);
	}

	protected abstract S execute( T input, U key ) throws E;
}

