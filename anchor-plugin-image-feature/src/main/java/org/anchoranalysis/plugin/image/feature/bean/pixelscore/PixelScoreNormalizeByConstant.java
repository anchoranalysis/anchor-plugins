/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;

public class PixelScoreNormalizeByConstant extends PixelScoreSingleChnl {

    // START BEAN PROPERTIES
    @BeanField private double value = 255;
    // END BEAN PROPERTIES

    @Override
    protected double deriveScoreFromPixelVal(int pixelVal) {
        return pixelVal / value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
