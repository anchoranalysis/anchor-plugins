package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

public class ShapeRegularityCenterSlice extends FeatureSingleObject {

	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
				
		try {
			ObjectMask omSlice = centerSlice(
				input.get().getObjectMask()
			);
			
			return ShapeRegularityCalculator.calcShapeRegularity(omSlice);
			
		} catch (OperationFailedException e1) {
			throw new FeatureCalcException(e1);
		}
	}
	
	private ObjectMask centerSlice( ObjectMask om ) throws OperationFailedException {
		int zSliceCenter = (int) om.centerOfGravity().getZ();
		return  om.extractSlice(
			zSliceCenter - om.getBoundingBox().getCornerMin().getZ(),
			false
		);
	}
}
