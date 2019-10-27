package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpair;

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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNghbMask;

/**
 * A simple scheme for counting the touching voxels.
 * 
 *  A voxel in the second object is touching if it has 4-connectivity with a voxel on the exterior of the first-object (source)
 *
 * In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersection. So as not to waste computation-time in useless areas. 
 * 
 * @author Owen Feehan
 *
 */
public class NumTouchingVoxels extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean use3D = true;
	// END BEAN PROPERTIES
	
	private CachedCalculation<BoundingBox> cc;
	
	@Override
	public void beforeCalc(FeatureInitParams params,
			CacheSession cache) throws InitException {
		super.beforeCalc(params, cache);
		this.cc = cache.search( new CalculateIntersectionOfDilatedBoundingBox(use3D) );
	}
	
	private int calcNghbTouchingPixels( ObjMask omSrc, ObjMask omDest, BoundingBox bboxIntersectRel ) throws OperationFailedException {
		
		ObjMask omOtherDest = NumTouchingVoxelFaces.createRelMask( omSrc, omDest );
		
		CountKernelNghbMask kernelMatch = new CountKernelNghbMask(use3D, omSrc.getBinaryValuesByte(), omOtherDest, false );
		return ApplyKernel.applyForCount(kernelMatch, omSrc.getVoxelBox(), bboxIntersectRel );		
	}
	
	@Override
	public double calcCast(FeatureObjMaskPairParams params)
			throws FeatureCalcException {

		try {
			ObjMask om1 = params.getObjMask1();
			ObjMask om2 = params.getObjMask2();
			
			// This is relative to Om1
			BoundingBox bboxIntersect = cc.getOrCalculate(params);
			
			BoundingBox bboxIntersectRelOm1 = NumTouchingVoxelFaces.createRelBBox(bboxIntersect, om1);
			BoundingBox bboxIntersectRelOm2 = NumTouchingVoxelFaces.createRelBBox(bboxIntersect, om2);
			
			// As this means of measuring the touching pixels can differ slightly depending on om1->om2 or om2->om1
			//  we calculate both and take the mean
			int fromOm1 = calcNghbTouchingPixels( om1, om2, bboxIntersectRelOm1 );
			
			// We change the bboxIntersectRel to be relative instead to om2
//			{
//				bboxIntersectRel.getCrnrMin().add( om1.getBoundingBox().getCrnrMin() );
//				bboxIntersectRel.setCrnrMin( bboxIntersectRel.relPosTo(om2.getBoundingBox()) );
//				bboxIntersectRel.clipTo( om2.getBoundingBox().extnt() );
//			}
			
			int fromOm2 = calcNghbTouchingPixels( om2, om1, bboxIntersectRelOm2 );
			
			return Math.max(fromOm1, fromOm2);
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}

//		// OLD METHOD
//		ObjMask om1Grown = ObjMaskProviderDilate.createDilatedObjMask(om1, e, true, 1);
//		ObjMask om2Grown = ObjMaskProviderDilate.createDilatedObjMask(om2, e, true, 1);
//		int val= om1Grown.countIntersectingPixels(om2);
//		int val2 = om2Grown.countIntersectingPixels(om1);
	}






}
