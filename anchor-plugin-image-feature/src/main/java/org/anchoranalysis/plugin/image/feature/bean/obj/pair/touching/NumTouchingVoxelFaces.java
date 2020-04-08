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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;

/**
 * A scheme for counting the touching voxels by intersection of object-masks
 * 
 * <p>Specifically, one of the object-masks is dilated, and count the number of intersecting pixels
 * with another mask.</p>
 * 
 * <p>However, intersection(a*,b)!=intersection(a,b*) where * is the dilation operator.
 * Different counts occur as a single-voxel can have multiple edges with the neighbour.</p>
 * 
 * <p>So it's better if we can iterate with a kernel over the edge pixels, and count the number of neighbours
 * 
 * We do this only where the bounding-boxes (dilated by 1 pixels) intersection. So as not to waste computation-time in useless areas.</p>
 * 
 * @author Owen Feehan
 *
 */
public class NumTouchingVoxelFaces extends TouchingVoxels {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskPairParams> paramsCacheable)
			throws FeatureCalcException {

		FeatureObjMaskPairParams params = paramsCacheable.getParams();
		
		try {
			ObjMask om1 = params.getObjMask1();
			ObjMask om2 = params.getObjMask2();
			
			BoundingBox bboxIntersect = bboxIntersectDilated(paramsCacheable);
			
			if (bboxIntersect==null) {
				// No intersection so no touching voxel faces
				return 0;
			}
			
			BoundingBox bboxIntersectRel = RelativeUtilities.createRelBBox(
				bboxIntersect,
				om1
			);
			
			ObjMask om2Rel = RelativeUtilities.createRelMask( om1, om2 );
			
			return ApplyKernel.applyForCount(
				createCountKernelMask(om1, om2Rel),
				om1.getVoxelBox(),
				bboxIntersectRel
			);
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}
}