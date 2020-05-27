package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

import java.util.Optional;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNghbMask;


/**
 * Base class for features that calculate touching with a dilated bounding box intersection
 * 
 * @author owen
 *
 */
public abstract class TouchingVoxels extends FeatureObjMaskPair {

	// START BEAN PROPERTIES
	@BeanField
	private boolean use3D = true;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputPairObjs> input) throws FeatureCalcException {

		FeatureInputPairObjs inputSessionless = input.get();
		
		Optional<BoundingBox> bboxIntersect = bboxIntersectDilated(input);
		
		if (!bboxIntersect.isPresent()) {
			// No intersection, so therefore return 0
			return 0;
		}
		
		return calcWithIntersection(
			inputSessionless.getFirst(),
			inputSessionless.getSecond(),
			bboxIntersect.get()
		);
	}
	
	protected abstract double calcWithIntersection(ObjMask om1, ObjMask om2, BoundingBox bboxIntersect) throws FeatureCalcException;
	
	/** The intersection of the bounding box of one mask with the (dilated by 1 bounding-box) of the other */
	private Optional<BoundingBox> bboxIntersectDilated(SessionInput<FeatureInputPairObjs> input) throws FeatureCalcException {
		return input.calc(
			new CalculateIntersectionOfDilatedBoundingBox(use3D)	
		);
	}
	
	protected CountKernel createCountKernelMask( ObjMask om1, ObjMask om2Rel ) {
		return new CountKernelNghbMask(use3D, om1.getBinaryValuesByte(), om2Rel, true );
	}
	
	public boolean isUse3D() {
		return use3D;
	}

	public void setUse3D(boolean use3D) {
		use3D = use3D;
	}
}
