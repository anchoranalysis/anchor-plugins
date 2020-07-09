package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

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

import java.util.Optional;

import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
class CalculateObjForDerived extends CalculateInputFromDelegateOption<
	FeatureInputSingleObject,
	FeatureInputSingleObject,
	ObjectMask
> {
	public CalculateObjForDerived(ResolvedCalculation<ObjectMask, FeatureInputSingleObject> ccDerived) {
		super(ccDerived);
	}

	@Override
	protected Optional<FeatureInputSingleObject> deriveFromDelegate(FeatureInputSingleObject input, ObjectMask delegate) {
		
		if (delegate==null || !delegate.hasPixelsGreaterThan(0)) {
			return Optional.empty();
		}
		
		return Optional.of(
			new FeatureInputSingleObject( delegate, input.getNrgStackOptional() )
		);
	}
}
