package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;

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

import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;
import org.anchoranalysis.image.feature.bean.FeatureShared;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.nrg.FeatureInputNRGStackDescriptor;

/**
 * Calculates as object-masks from entities in shared, using the feature-input only for a nrg-stack.
 * 
 * @author Owen Feehan
 *
 * @param <T> feature-input
 */
public abstract class FeatureSingleObjectFromShared<T extends FeatureInputNRG> extends FeatureShared<T> {

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleObject> item;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<T> input) throws FeatureCalcException {
		return calc(input.forChild(), item);
	}
	
	protected abstract double calc(CalcForChild<T> calcForChild, Feature<FeatureInputSingleObject> featureForSingleObject) throws FeatureCalcException;
	
	@Override
	public FeatureInputDescriptor inputDescriptor() {
		return FeatureInputNRGStackDescriptor.INSTANCE;
	}
	
	public Feature<FeatureInputSingleObject> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleObject> item) {
		this.item = item;
	}
}
