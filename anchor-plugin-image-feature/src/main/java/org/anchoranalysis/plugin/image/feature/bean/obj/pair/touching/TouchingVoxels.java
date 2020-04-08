package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean use3D = true;
	// END BEAN PROPERTIES
		
	/** The intersection of the bounding box of one mask with the (dilated by 1 bounding-box) of the other */
	protected BoundingBox bboxIntersectDilated( CacheableParams<FeatureObjMaskPairParams> params) throws ExecuteException {
		return params.calc(
			new CalculateIntersectionOfDilatedBoundingBox(use3D)	
		);
	}
	
	protected CountKernel createCountKernelMask( ObjMask om1, ObjMask om2Rel ) {
		return new CountKernelNghbMask(use3D, om1.getBinaryValuesByte(), om2Rel, true );
	}
	
	public boolean isUse3D() {
		return use3D;
	}

	public void setUse3D(boolean use3d) {
		use3D = use3d;
	}
}