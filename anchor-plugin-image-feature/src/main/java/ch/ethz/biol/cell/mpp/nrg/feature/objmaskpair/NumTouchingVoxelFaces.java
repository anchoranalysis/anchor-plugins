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
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNghbMask;

/**
 * A simple scheme for counting the touching voxels, is to dilate one of the object-masks, and count the number of intersecting pixels with another mask.
 * 
 * However, intersection(a*,b)!=intersection(a,b*) where * is the dilation operator. Different counts occur as a single-voxel can have multiple edges with the neighbour.
 * 
 * So it's better if we can iterate with a kernel over the edge pixels, and count the number of neighbours
 * 
 * We do this only where the bounding-boxes (dilated by 1 pixels) intersection. So as not to waste computation-time in useless areas.
 * 
 * @author Owen Feehan
 *
 */
public class NumTouchingVoxelFaces extends FeatureObjMaskPair {

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
	
	static BoundingBox createRelBBox( BoundingBox bboxIntersect, ObjMask om1 ) {
		BoundingBox bboxIntersectRel = new BoundingBox( bboxIntersect.relPosTo(om1.getBoundingBox()), bboxIntersect.extnt() );
		bboxIntersectRel.clipTo( om1.getBoundingBox().extnt() );
		return bboxIntersectRel;
	}
	
	static ObjMask createRelMask( ObjMask om1, ObjMask om2 ) {
		ObjMask om2Rel = om2.relMaskTo(om1.getBoundingBox());
		om2Rel.getBoundingBox().getCrnrMin().scale(-1);
		return om2Rel;
	}
	
	@Override
	public double calcCast(FeatureObjMaskPairParams params)
			throws FeatureCalcException {

		try {
			ObjMask om1 = params.getObjMask1();
			ObjMask om2 = params.getObjMask2();
			
			BoundingBox bboxIntersect = cc.getOrCalculate(params);
			BoundingBox bboxIntersectRel = createRelBBox(bboxIntersect, om1);
			
			ObjMask om2Rel = createRelMask( om1, om2 );
			
			CountKernel kernelMatch = new CountKernelNghbMask(use3D, om1.getBinaryValuesByte(), om2Rel, true );
			return ApplyKernel.applyForCount(kernelMatch, om1.getVoxelBox(), bboxIntersectRel );
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}

}
