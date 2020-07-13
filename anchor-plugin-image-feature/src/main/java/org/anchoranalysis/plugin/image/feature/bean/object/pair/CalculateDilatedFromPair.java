package org.anchoranalysis.plugin.image.feature.bean.object.pair;

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

import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/** Calculates a dilated-object from a pair */
@AllArgsConstructor(access=AccessLevel.PRIVATE) @EqualsAndHashCode(callSuper = false)
class CalculateDilatedFromPair extends FeatureCalculation<ObjectMask, FeatureInputPairObjects> {
	
	// Not included in hash-coding as its assumed to be singular
	@EqualsAndHashCode.Exclude
	private CalcForChild<FeatureInputPairObjects> resolverForChild;

	private ResolvedCalculation<FeatureInputSingleObject, FeatureInputPairObjects> calcInput;
	private Extract extract;
	private ChildCacheName childCacheName;
	private int iterations;
	private boolean do3D;
	
	public static FeatureCalculation<ObjectMask, FeatureInputPairObjects> createFromCache(
		CalculationResolver<FeatureInputPairObjects> resolver,
		CalcForChild<FeatureInputPairObjects> calcForChild,
		Extract extract,
		ChildCacheName childCacheName,
		int iterations,
		boolean do3D
	) {
		return new CalculateDilatedFromPair(
			calcForChild,
			resolver.search(
				new CalculateInputFromPair(extract)
			),
			extract,
			childCacheName,
			iterations,
			do3D
		);
	}

	@Override
	protected ObjectMask execute(FeatureInputPairObjects input) throws FeatureCalcException {
		return resolverForChild.calc(
			childCacheName,
			calcInput.getOrCalculate(input),
			resolver -> CalculateDilation.createFromCache(
				resolver,
				iterations,
				do3D
			)
		);
	}
}
