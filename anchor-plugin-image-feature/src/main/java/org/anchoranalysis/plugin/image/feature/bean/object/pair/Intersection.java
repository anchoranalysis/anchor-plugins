package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import java.util.Optional;


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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

import lombok.Getter;
import lombok.Setter;

/**
 * Finds the intersection of two objects and calculates a feature on it
 *  
 * @author Owen Feehan
 *
 */
public class Intersection extends FeatureDeriveFromPair {

	// START BEAN PROPERTIES
	@BeanField @Positive @Getter @Setter
	private int iterationsDilation = 0;
	
	@BeanField @Getter @Setter
	private int iterationsErosion = 0;
		
	@BeanField @Getter @Setter
	private boolean do3D = true;
	
	@BeanField @Getter @Setter
	private double emptyValue = 255;
	// END BEAN PROPERTIES
		
	@Override
	public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {
		
		return CalculateInputFromDelegateOption.calc(
			input,
			createCalculation(input),
			CalculateIntersectionInput::new,
			getItem(),
			cacheIntersectionName(),
			emptyValue
		);
	}
	
	/** A unique cache-name for the intersection of how we find a parameterization */
	private ChildCacheName cacheIntersectionName() {
		String id = String.format(
			"intersection_%d_%d_%d",
			iterationsDilation,
			iterationsErosion,
			do3D ? 1 : 0
		);
		return new ChildCacheName(Intersection.class, id);
	}

	private FeatureCalculation<Optional<ObjectMask>,FeatureInputPairObjects> createCalculation(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {
		try {
			return CalculatePairIntersectionCommutative.createFromCache(
				input,
				CACHE_NAME_FIRST,
				CACHE_NAME_SECOND,
				iterationsDilation,
				iterationsErosion,
				do3D
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
}
