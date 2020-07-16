/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.scale;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

public class ScaleCalculatorRelativeDimensions extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private ImageDimProvider dimSource;

    @BeanField private ImageDimProvider dimTarget;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calc(ImageDimensions srcDim) throws OperationFailedException {

        ImageDimensions sdSource = srcDim;
        if (dimSource != null) {
            try {
                sdSource = dimSource.create();
            } catch (CreateException e) {
                throw new OperationFailedException(e);
            }
        }

        if (sdSource == null) {
            throw new OperationFailedException("No source dimensions can be found");
        }

        try {
            return ScaleFactorUtilities.calcRelativeScale(
                    sdSource.getExtent(), dimTarget.create().getExtent());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    public ImageDimProvider getDimSource() {
        return dimSource;
    }

    public void setDimSource(ImageDimProvider dimSource) {
        this.dimSource = dimSource;
    }

    public ImageDimProvider getDimTarget() {
        return dimTarget;
    }

    public void setDimTarget(ImageDimProvider dimTarget) {
        this.dimTarget = dimTarget;
    }
}
