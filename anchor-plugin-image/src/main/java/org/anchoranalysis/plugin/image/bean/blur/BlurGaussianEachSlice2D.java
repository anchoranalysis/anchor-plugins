/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.blur;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Performs a Gaussian-blur in 2D on each slice independently
 *
 * @author Owen Feehan
 */
public class BlurGaussianEachSlice2D extends BlurStrategy {

    @Override
    public void blur(VoxelBoxWrapper voxelBox, ImageDimensions dimensions, MessageLogger logger)
            throws OperationFailedException {

        double sigma = calcSigma(dimensions, logger);

        Extent e = voxelBox.any().extent();
        double[] sigmaArr = new double[] {sigma, sigma};

        for (int z = 0; z < e.getZ(); z++) {

            GaussianBlurUtilities.applyBlur(
                    ImgLib2Wrap.wrap(voxelBox.any().getPixelsForPlane(z), e),
                    dimensions.getRes(),
                    sigmaArr);
        }
    }
}
