package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

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
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNghbMask;

/**
 * A scheme for counting touching voxels.
 * 
 * <p>A voxel in the second object is deemed touching if it has 4-connectivity with a voxel on the exterior
 * of the first-object (source)</p>
 *
 * <p>In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersection, so as to
 * reduce computation-time.</p> 
 * 
 * @author Owen Feehan
 *
 */
public class NumTouchingVoxels extends TouchingVoxels {

	@Override
	protected double calcWithIntersection(ObjectMask object1, ObjectMask object2, BoundingBox bboxIntersect)
			throws FeatureCalcException {
		// As this means of measuring the touching pixels can differ slightly depending on om1->om2 or om2->om1, it's done in both directions.
		try {
			return Math.max(
				numTouchingFrom(object1, object2, bboxIntersect),
				numTouchingFrom(object2, object1, bboxIntersect)
			);
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	private int numTouchingFrom(
		ObjectMask source,
		ObjectMask destination,
		BoundingBox bboxIntersect
	) throws OperationFailedException {
		BoundingBox bboxIntersectRelative = RelativeUtilities.createRelBBox(bboxIntersect, source);
		return calcNghbTouchingPixels( source, destination, bboxIntersectRelative );
	}
		
	private int calcNghbTouchingPixels(
		ObjectMask source,
		ObjectMask destination,
		BoundingBox bboxIntersectRelative
	) throws OperationFailedException {
		
		CountKernelNghbMask kernelMatch = new CountKernelNghbMask(
			isDo3D(),
			source.getBinaryValuesByte(),
			RelativeUtilities.createRelMask( destination, source ),
			false
		);
		return ApplyKernel.applyForCount(kernelMatch, source.getVoxelBox(), bboxIntersectRelative );		
	}


}
