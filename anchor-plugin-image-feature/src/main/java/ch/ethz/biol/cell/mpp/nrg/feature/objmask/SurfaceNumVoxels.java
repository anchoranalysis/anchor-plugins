package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.plugin.image.calculation.CalculateOutlineNumVoxels;

/**
 * The number of voxels on the surface (all voxels on the exterior of the object)
 * 
 * @author Owen Feehan
 *
 */
public class SurfaceNumVoxels extends SurfaceNumVoxelsBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected FeatureCalculation<Integer, FeatureInputSingleObj> createParams(boolean mip, boolean suppress3d) {
		return new CalculateOutlineNumVoxels(mip, suppress3d);
	}
}
