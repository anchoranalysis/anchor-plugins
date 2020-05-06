package org.anchoranalysis.plugin.image.calculation;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateErosion extends CalculateObjMask {

	private CalculateErosion(
		int iterations,
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map
	) {
		super(iterations,map);
	}
	
	private CalculateErosion( CalculateObjMask src ) {
		super(src);
	}
	
	public static FeatureCalculation<ObjMask,FeatureInputSingleObj> create(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		ResolvedCalculationMap<ObjMask,FeatureInputSingleObj,Integer> map = cache.search(
			new CalculateErosionMap(do3D)
		);
		
		return new CalculateErosion(iterations, map);
	}
	
	public static ResolvedCalculation<ObjMask,FeatureInputSingleObj> createFromCacheRslvd(
		CalculationResolver<FeatureInputSingleObj> cache,
		int iterations,
		boolean do3D
	) {
		return cache.search(
			create(cache, iterations, do3D)
		);
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateErosion){
	        return super.equals(obj);
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper( super.hashCode() ).hashCode();
	}
}
