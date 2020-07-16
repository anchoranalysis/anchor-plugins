/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreRejectIfIntensityLessThan extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private PixelScore item;

    @BeanField private int nrgChnlIndex = 0;

    @BeanField private int minIntensity = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        int intensity = pixelVals[nrgChnlIndex];

        if (intensity < minIntensity) {
            return 0;
        }

        return item.calc(pixelVals);
    }

    public PixelScore getItem() {
        return item;
    }

    public void setItem(PixelScore item) {
        this.item = item;
    }

    public int getNrgChnlIndex() {
        return nrgChnlIndex;
    }

    public void setNrgChnlIndex(int nrgChnlIndex) {
        this.nrgChnlIndex = nrgChnlIndex;
    }

    public int getMinIntensity() {
        return minIntensity;
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity = minIntensity;
    }
}
