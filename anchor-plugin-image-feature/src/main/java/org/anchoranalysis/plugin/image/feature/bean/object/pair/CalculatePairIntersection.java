package org.anchoranalysis.plugin.image.feature.bean.object.pair;

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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Procedure to calculate an area of intersection between two objects (termed first and second)
 * <p>
 * Specifically, this is achieved by:
 * <ul>
 * <li>dilating {@code first} by {@code iterationsFirst}
 * <li>dilating {@code second} by {@code iterationsSecond}
 * <li>finding the intersection of 1. and 2.
 * <li>then eroding by {@code iterationsErosion}
 * </ul> 
 * <p>
 * This is NOT commutative: {@code f(a,b)!=f(b,a)}
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor @EqualsAndHashCode(callSuper=false)
class CalculatePairIntersection extends FeatureCalculation<Optional<ObjectMask>,FeatureInputPairObjects> {

	private final boolean do3D;
	private final int iterationsErosion;
	private final ResolvedCalculation<ObjectMask,FeatureInputPairObjects> first;
	private final ResolvedCalculation<ObjectMask,FeatureInputPairObjects> second;
	
	public static ResolvedCalculation<Optional<ObjectMask>,FeatureInputPairObjects> of(
		SessionInput<FeatureInputPairObjects> cache,
		ChildCacheName cacheLeft,
		ChildCacheName cacheRight,
		int iterationsFirst,
		int iterationsSecond,
		boolean do3D,
		int iterationsErosion
	) {
		// We use two additional caches, for the calculations involving the single objects, as these can be expensive, and we want
		//  them also cached
		return cache.resolver().search(
			new CalculatePairIntersection(
				do3D,
				iterationsErosion,
				cache.resolver().search(
					CalculateDilatedFromPair.of(
						cache.resolver(),
						cache.forChild(),
						Extract.FIRST,
						cacheLeft,
						iterationsFirst,
						do3D
					)
				),
				cache.resolver().search(
					CalculateDilatedFromPair.of(
						cache.resolver(),
						cache.forChild(),
						Extract.SECOND,
						cacheRight,
						iterationsSecond,
						do3D
					)
				)
			)
		);
	}

	@Override
	protected Optional<ObjectMask> execute( FeatureInputPairObjects input ) throws FeatureCalcException {
	
		ImageDimensions dimensions = input.getDimensionsRequired();
				
		ObjectMask object1Dilated = first.getOrCalculate(input);
		ObjectMask object2Dilated = second.getOrCalculate(input);
		
		Optional<ObjectMask> omIntersection = object1Dilated.intersect(object2Dilated, dimensions );
				
		if (!omIntersection.isPresent()) {
			return Optional.empty();
		}
		
		assert( omIntersection.get().hasPixelsGreaterThan(0) );
		
		try {
			if (iterationsErosion>0) {
				return erode(input, omIntersection.get(), dimensions);
			} else {
				return omIntersection;
			}
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	private Optional<ObjectMask> erode(
		FeatureInputPairObjects input,
		ObjectMask intersection,
		ImageDimensions dimensions
	) throws CreateException {
		
		// We erode it, and use this as a mask on the input object
		ObjectMask eroded = MorphologicalErosion.createErodedObject(
			input.getMerged(),
			Optional.of(dimensions.getExtent()),
			do3D,
			iterationsErosion,
			true,
			Optional.empty()
		);
		
		return intersection.intersect(eroded, dimensions);
	}
}
