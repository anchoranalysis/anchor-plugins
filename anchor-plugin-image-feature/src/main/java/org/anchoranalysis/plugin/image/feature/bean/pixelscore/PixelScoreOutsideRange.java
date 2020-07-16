/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreOutsideRange extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private int min = 0;

    @BeanField private int max = 256;

    @BeanField private int nrgIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        double val = pixelVals[nrgIndex];
        if (val < min) {
            return 0;
        }
        if (val > max) {
            return 255;
        }
        return val;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getNrgIndex() {
        return nrgIndex;
    }

    public void setNrgIndex(int nrgIndex) {
        this.nrgIndex = nrgIndex;
    }
}
