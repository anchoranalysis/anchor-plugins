/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.blur;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * A method for applying blurring to an image
 *
 * @author Owen Feehan
 */
public abstract class BlurStrategy extends AnchorBean<BlurStrategy> {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private double sigma = 3;

    @BeanField @Getter @Setter
    private boolean sigmaInMeters = false; // Treats sigma if it's microns
    // END BEAN PROPERTIES

    public abstract void blur(
            VoxelBoxWrapper voxelBox, ImageDimensions dimensions, MessageLogger logger)
            throws OperationFailedException;

    protected double calcSigma(ImageDimensions dimensions, MessageLogger logger)
            throws OperationFailedException {

        double sigmaToUse = sigma;

        if (sigmaInMeters) {
            // Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of
            // later)
            sigmaToUse = ImageUnitConverter.convertFromMeters(sigma, dimensions.getRes());

            logger.logFormatted("Converted sigmaInMeters=%f into sigma=%f", sigma, sigmaToUse);
        }

        if (sigmaToUse > dimensions.getX() || sigmaToUse > dimensions.getY()) {
            throw new OperationFailedException(
                    "The calculated sigma is FAR TOO LARGE. It is larger than the entire channel it is applied to");
        }

        return sigmaToUse;
    }
}
