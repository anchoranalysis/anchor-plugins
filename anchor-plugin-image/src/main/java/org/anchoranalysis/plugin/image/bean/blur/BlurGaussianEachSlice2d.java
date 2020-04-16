package org.anchoranalysis.plugin.image.bean.blur;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Performs a Gaussian-blur in 2D on each slice independently
 * 
 * @author Owen Feehan
 *
 */
public class BlurGaussianEachSlice2d extends BlurStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void blur( VoxelBoxWrapper voxelBox, ImageDim dim, LogReporter logger ) throws OperationFailedException {
		
		double sigma = calcSigma(dim, logger);
		
		Extent e = voxelBox.any().extnt();
		double[] sigmaArr = new double[]{ sigma, sigma };
		
		for( int z=0; z<e.getZ(); z++) {

			GaussianBlurUtilities.applyBlur(
				ImgLib2Wrap.wrap( voxelBox.any().getPixelsForPlane(z), e ),
				dim.getRes(),
				sigmaArr
			);
		}
	}
}