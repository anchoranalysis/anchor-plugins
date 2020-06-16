package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;

/**
 * A simple scheme for counting the touching voxels.
 * 
 *  A voxel in the second object is touching if it has 4-connectivity with a voxel on the exterior of the first-object (source)
 *
 * In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersect. So as not to waste computation-time in useless areas. 
 * 
 * @author Owen Feehan
 *
 */
public class HasTouchingVoxels extends TouchingVoxels {

	@Override
	protected double calcWithIntersection(ObjectMask om1, ObjectMask om2, BoundingBox bboxIntersect)
			throws FeatureCalcException {
		return convertToInt(
			calculateHasTouchingRelative(
				om1,
				RelativeUtilities.createRelMask( om2, om1 ),
				RelativeUtilities.createRelBBox(bboxIntersect, om1)
			)
		);
	}

	private boolean calculateHasTouchingRelative(ObjectMask om1, ObjectMask om2Rel, BoundingBox bboxIntersectRel) throws FeatureCalcException {
		CountKernel kernelMatch = createCountKernelMask(om1, om2Rel);
		try {
			return ApplyKernel.applyUntilPositive(kernelMatch, om1.getVoxelBox(), bboxIntersectRel );
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}

	private static int convertToInt( boolean b) {
		return b ? 1 : 0;
	}
}
