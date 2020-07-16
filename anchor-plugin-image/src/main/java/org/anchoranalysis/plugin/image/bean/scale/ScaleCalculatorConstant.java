/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.scale;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ScaleCalculatorConstant extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField private double value;
    // END BEAN PROPERTIES

    @Override
    public ScaleFactor calc(ImageDimensions srcDim) throws OperationFailedException {
        return new ScaleFactor(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
