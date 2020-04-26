package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.plugin.image.calculation.CalculateOutlineNumVoxelFaces;

/**
 * The number of voxel-faces along the surface (the faces of each voxel that touch outside)
 * 
 * @author Owen Feehan
 *
 */
public class SurfaceNumVoxelFaces extends SurfaceNumVoxelsBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected FeatureCalculation<Integer, FeatureInputSingleObj> createParams(boolean mip, boolean suppress3d) {
		return new CalculateOutlineNumVoxelFaces(mip, suppress3d);
	}
}
