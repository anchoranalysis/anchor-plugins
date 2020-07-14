package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;





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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

import lombok.Getter;
import lombok.Setter;

public abstract class DerivedObject extends FeatureSingleObject {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private double emptyValue = 255;
	
	@BeanField @Getter @Setter
	private Feature<FeatureInputSingleObject> item;
	// END BEAN PROPERTIES

	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

		ChildCacheName cacheName = cacheName();
		
		return CalculateInputFromDelegateOption.calc(
			input,
			createCachedCalculationForDerived(
				input.resolver()
			),
			CalculateObjForDerived::new,
			item,
			cacheName,
			emptyValue
		);
	}
		
	protected abstract FeatureCalculation<ObjectMask,FeatureInputSingleObject> createCachedCalculationForDerived( CalculationResolver<FeatureInputSingleObject> session ) throws FeatureCalcException;
	
	protected abstract ChildCacheName cacheName();
}
