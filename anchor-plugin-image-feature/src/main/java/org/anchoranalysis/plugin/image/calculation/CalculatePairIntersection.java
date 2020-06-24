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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.feature.objmask.pair.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.morph.MorphologicalErosion;
import org.anchoranalysis.image.objectmask.ops.ObjectMaskMerger;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Combines two object-masks by:
 *   1. dilating objMask1 by iterations1
 *   2. dilating objMask2 by iterations2
 *   3. finding the intersection of 1. and 2.
 *   4. then eroding by iterationsErosion 
 * 
 *  This is NOT commutative:  f(a,b)==f(b,a)
 * 
 * @author Owen Feehan
 *
 */
public class CalculatePairIntersection extends FeatureCalculation<Optional<ObjectMask>,FeatureInputPairObjs> {

	private boolean do3D;
	private int iterationsErosion;
	
	private ResolvedCalculation<ObjectMask,FeatureInputPairObjs> left;
	private ResolvedCalculation<ObjectMask,FeatureInputPairObjs> right;
	
	public static ResolvedCalculation<Optional<ObjectMask>,FeatureInputPairObjs> createFromCache(
		SessionInput<FeatureInputPairObjs> cache,
		ChildCacheName cacheChildName1,
		ChildCacheName cacheChildName2,
		int iterations1,
		int iterations2,
		boolean do3D,
		int iterationsErosion
	) throws CreateException {
		
		// We use two additional caches, for the calculations involving the single objects, as these can be expensive, and we want
		//  them also cached
		return cache.resolver().search(
			new CalculatePairIntersection(
				do3D,
				iterationsErosion,
				cache.resolver().search(
					new CalculateDilatedFromPair(
						cache.resolver(),
						cache.forChild(),
						Extract.FIRST,
						cacheChildName1,
						iterations1,
						do3D
					)
				),
				cache.resolver().search(
					new CalculateDilatedFromPair(
						cache.resolver(),
						cache.forChild(),
						Extract.SECOND,
						cacheChildName2,
						iterations2,
						do3D
					)
				)
			)
		);
	}
		
	private CalculatePairIntersection(
		boolean do3D,
		int iterationsErosion,
		ResolvedCalculation<ObjectMask,FeatureInputPairObjs> left,
		ResolvedCalculation<ObjectMask,FeatureInputPairObjs> right
	) {
		super();
		this.iterationsErosion = iterationsErosion;
		this.do3D = do3D;
		this.left = left;
		this.right = right;
	}

	@Override
	protected Optional<ObjectMask> execute( FeatureInputPairObjs input ) throws FeatureCalcException {
	
		ImageDim dim = input.getDimensionsRequired();
				
		ObjectMask om1Dilated = left.getOrCalculate(input);
		ObjectMask om2Dilated = right.getOrCalculate(input);
		
		Optional<ObjectMask> omIntersection = om1Dilated.intersect(om2Dilated, dim );
				
		if (!omIntersection.isPresent()) {
			return Optional.empty();
		}
		
		assert( omIntersection.get().hasPixelsGreaterThan(0) );
		
		try {
			if (iterationsErosion>0) {
				return erode(input, omIntersection.get(), dim);
			} else {
				return omIntersection;
			}
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculatePairIntersection){
	        final CalculatePairIntersection other = (CalculatePairIntersection) obj;
	        return new EqualsBuilder()
	            .append(iterationsErosion, other.iterationsErosion)
	            .append(do3D, other.do3D)
	            .append(left, other.left)
	            .append(right, other.right)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(iterationsErosion)
			.append(do3D)
			.append(left)
			.append(right)
			.toHashCode();
	}
	
	private Optional<ObjectMask> erode( FeatureInputPairObjs params, ObjectMask omIntersection, ImageDim dim ) throws CreateException {

		ObjectMask omMerged = params.getMerged();
		
		// We merge the two masks, and then erode it, and use this as a mask on the input object
		if (omMerged==null) {
			omMerged = ObjectMaskMerger.merge( params.getFirst(), params.getSecond() );
		}
		
		ObjectMask omMergedEroded = MorphologicalErosion.createErodedObjMask(
			omMerged,
			Optional.of(dim.getExtnt()),
			do3D,
			iterationsErosion,
			true,
			Optional.empty()
		);
		
		Optional<ObjectMask> omIntersect = omIntersection.intersect(omMergedEroded, dim);
		omIntersect.ifPresent( om-> { assert( om.hasPixelsGreaterThan(0) ); });
		return omIntersect;
	}


}
