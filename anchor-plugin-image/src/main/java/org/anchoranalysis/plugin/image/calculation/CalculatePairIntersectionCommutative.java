package org.anchoranalysis.plugin.image.calculation;

import java.util.Optional;

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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Finds the intersection between the dilated versions of two objects (and then performs some erosion)
 * 
 * 1. Each object-mask is dilated by (determined by iterationsDilation)
 * 2. The intersection is found
 * 3. Then erosion occurs (determined by iterationsErosion) 
 * 
 * This is commutative:  f(a,b)==f(b,a)
 * 
 * See {@link CalculatePairIntersection
 * 
 * @author Owen Feehan
 *
 */
public class CalculatePairIntersectionCommutative extends CacheableCalculation<Optional<ObjMask>,FeatureInputPairObjs> {

	private ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccFirstToSecond;
	private ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccSecondToFirst;
	
	public static CacheableCalculation<Optional<ObjMask>,FeatureInputPairObjs> createFromCache(
		CalculationResolver<FeatureInputPairObjs> cache,
		CalculationResolver<FeatureInputSingleObj> cacheDilationObj1,
		CalculationResolver<FeatureInputSingleObj> cacheDilationObj2,
		int iterationsDilation,
		int iterationsErosion,
		boolean do3D
	) throws CreateException {
		
		// We use two additional caches, for the calculations involving the single objects, as these can be expensive, and we want
		//  them also cached
		ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccFirstToSecond = CalculatePairIntersection.createFromCache(
			cache, cacheDilationObj1, cacheDilationObj2, iterationsDilation, 0, do3D, iterationsErosion	
		);
		ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccSecondToFirst = CalculatePairIntersection.createFromCache(
			cache, cacheDilationObj1, cacheDilationObj2, 0, iterationsDilation, do3D, iterationsErosion	
		);
		return new CalculatePairIntersectionCommutative(ccFirstToSecond, ccSecondToFirst);
	}
	
	private CalculatePairIntersectionCommutative(
		ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccFirstToSecond,
		ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> ccSecondToFirst
	) {
		super();
		this.ccFirstToSecond = ccFirstToSecond;
		this.ccSecondToFirst = ccSecondToFirst;
	}

	@Override
	protected Optional<ObjMask> execute(FeatureInputPairObjs params)
			throws ExecuteException {
		
		Optional<ObjMask> omIntersection1 = ccFirstToSecond.getOrCalculate(params);
		Optional<ObjMask> omIntersection2 = ccSecondToFirst.getOrCalculate(params);
		
		if (!omIntersection1.isPresent()) {
			return omIntersection2;
		}
		
		if (!omIntersection2.isPresent()) {
			return omIntersection1;
		}
		
		assert(omIntersection1.get().hasPixelsGreaterThan(0));
		assert(omIntersection2.get().hasPixelsGreaterThan(0));
		
		ObjMask merged = ObjMaskMerger.merge( omIntersection1.get(), omIntersection2.get() );
		assert(merged.hasPixelsGreaterThan(0));
		return Optional.of(merged);
	}

	@Override
	public boolean equals(Object obj) {
		 if(obj instanceof CalculatePairIntersectionCommutative){
		        final CalculatePairIntersectionCommutative other = (CalculatePairIntersectionCommutative) obj;
		        return new EqualsBuilder()
		            .append(ccFirstToSecond, other.ccFirstToSecond)
		            .append(ccSecondToFirst, other.ccSecondToFirst)
		            .isEquals();
		    } else{
		        return false;
		    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ccFirstToSecond).append(ccSecondToFirst).toHashCode();
	}
}
