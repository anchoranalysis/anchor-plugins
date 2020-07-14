package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged;

/*-
 * #%L
 * anchor-plugin-image
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

import java.util.function.Function;

import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @EqualsAndHashCode(callSuper=false)
class CalculateSingleInputFromMerged extends FeatureCalculation<FeatureInputSingleObject, FeatureInputPairObjects> {

	/** this function is used for extracting a particular object from the FeatureObjMaskPairMergedParams */
	private final Function<FeatureInputPairObjects, ObjectMask> extractObjFunc;
	
	/** so as to avoid relying on hashCode() and equals() on extractObjFunc, this field is used as a unique ID instead for each type of lambda */
	private final ChildCacheName uniqueIDForFunction;

	@Override
	protected FeatureInputSingleObject execute(FeatureInputPairObjects input) {
		
		ObjectMask selected = extractObjFunc.apply(input);
		
		return new FeatureInputSingleObject( selected, input.getNrgStackOptional() );
	}
}
