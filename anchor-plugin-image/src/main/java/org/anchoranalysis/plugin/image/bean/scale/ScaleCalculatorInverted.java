/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.scale;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ScaleCalculatorInverted extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField private ScaleCalculator scaleCalculator;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calc(ImageDimensions srcDim) throws OperationFailedException {

        ScaleFactor sf = scaleCalculator.calc(srcDim);
        return sf.invert();
    }

    public ScaleCalculator getScaleCalculator() {
        return scaleCalculator;
    }

    public void setScaleCalculator(ScaleCalculator scaleCalculator) {
        this.scaleCalculator = scaleCalculator;
    }
}
