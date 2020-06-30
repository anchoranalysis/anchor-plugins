package org.anchoranalysis.plugin.image.feature.object.calculation.pair;

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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** Calculates a dilated-object from a pair */
class CalculateDilatedFromPair extends FeatureCalculation<ObjectMask, FeatureInputPairObjects> {

	
	private ResolvedCalculation<FeatureInputSingleObject, FeatureInputPairObjects> calcInput;

	// Not included in hash-coding as its assumed to be singular
	private CalcForChild<FeatureInputPairObjects> resolverForChild;
	private Extract extract;
	private ChildCacheName childCacheName;
	private int iterations;
	private boolean do3D;
	
	public CalculateDilatedFromPair(
		CalculationResolver<FeatureInputPairObjects> resolver,
		CalcForChild<FeatureInputPairObjects> calcForChild,
		Extract extract,
		ChildCacheName childCacheName,
		int iterations,
		boolean do3D
	) {
		super();
		this.calcInput = resolver.search(
			new CalculateInputFromPair(extract)
		);
		this.resolverForChild = calcForChild;
		this.extract = extract;
		this.childCacheName = childCacheName;
		this.iterations = iterations;
		this.do3D = do3D;
	}


	@Override
	protected ObjectMask execute(FeatureInputPairObjects input) throws FeatureCalcException {

		// Input for calculaitng dilation
		FeatureInputSingleObject inputSingle = calcInput.getOrCalculate(input); 
		
		return resolverForChild.calc(
			childCacheName,
			inputSingle,
			resolver -> CalculateDilation.createFromCache(
				resolver,
				iterations,
				do3D
			)
		);
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateDilatedFromPair){
	        final CalculateDilatedFromPair other = (CalculateDilatedFromPair) obj;
	        return new EqualsBuilder()
	            .append(calcInput, other.calcInput)
	            .append(extract, other.extract)
	            .append(childCacheName, other.childCacheName)
	            .append(iterations, other.iterations)
	            .append(do3D, other.do3D)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(calcInput)
			.append(extract)
			.append(childCacheName)
			.append(iterations)
			.append(do3D)
			.toHashCode();
	}
}
