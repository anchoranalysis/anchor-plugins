/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreIdentity extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private int nrgChnlIndex = 0;

    @BeanField private boolean normalize = true;

    @BeanField private double factorLow = 1.0;

    @BeanField private double factorHigh = 1.0;

    @BeanField private boolean keepExtremes = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        double pxlValue = pixelVals[nrgChnlIndex];

        if (normalize) {
            double val = pxlValue / 255;

            if (keepExtremes) {
                if (val == 0.0) {
                    return val;
                }
                if (val == 1.0) {
                    return val;
                }
            }

            if (val < 0.5) {
                return 0.5 - ((0.5 - val) * factorLow);
            } else if (val > 0.5) {
                return 0.5 + ((val - 0.5) * factorHigh);
            } else {
                return val;
            }

        } else {
            return pxlValue;
        }
    }

    public int getNrgChnlIndex() {
        return nrgChnlIndex;
    }

    public void setNrgChnlIndex(int nrgChnlIndex) {
        this.nrgChnlIndex = nrgChnlIndex;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public double getFactorLow() {
        return factorLow;
    }

    public void setFactorLow(double factorLow) {
        this.factorLow = factorLow;
    }

    public double getFactorHigh() {
        return factorHigh;
    }

    public void setFactorHigh(double factorHigh) {
        this.factorHigh = factorHigh;
    }

    public boolean isKeepExtremes() {
        return keepExtremes;
    }

    public void setKeepExtremes(boolean keepExtremes) {
        this.keepExtremes = keepExtremes;
    }
}
