package org.anchoranalysis.plugin.image.bean.blur;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Performs a Gaussian Blur in 3D
 * 
 * @author Owen Feehan
 *
 */
public class BlurGaussian3d extends BlurStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void blur( VoxelBoxWrapper voxelBox, ImageDim dim, LogReporter logger ) throws OperationFailedException {
		
		double sigma = calcSigma(dim, logger);
		
		GaussianBlurUtilities.applyBlur(
			ImgLib2Wrap.wrap( voxelBox ),
			dim.getRes(),
			new double[]{ sigma, sigma, sigma/dim.getRes().getZRelRes() }	// Sigma-array
		);
	}
}