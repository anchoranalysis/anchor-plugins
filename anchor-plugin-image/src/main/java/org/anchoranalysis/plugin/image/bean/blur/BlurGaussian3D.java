/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.blur;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Performs a Gaussian Blur in 3D
 *
 * @author Owen Feehan
 */
public class BlurGaussian3D extends BlurStrategy {

    @Override
    public void blur(VoxelBoxWrapper voxelBox, ImageDimensions dimensions, MessageLogger logger)
            throws OperationFailedException {

        double sigma = calcSigma(dimensions, logger);

        GaussianBlurUtilities.applyBlur(
                ImgLib2Wrap.wrap(voxelBox),
                dimensions.getRes(),
                new double[] {
                    sigma, sigma, sigma / dimensions.getRes().getZRelativeResolution()
                } // Sigma-array
                );
    }
}
