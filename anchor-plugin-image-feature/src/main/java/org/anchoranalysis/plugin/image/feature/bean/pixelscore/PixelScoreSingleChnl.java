/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

/**
 * A score that is calculated on a single channel only
 *
 * @author Owen Feehan
 */
public abstract class PixelScoreSingleChnl extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private int nrgChnlIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        return deriveScoreFromPixelVal(pixelVals[nrgChnlIndex]);
    }

    protected abstract double deriveScoreFromPixelVal(int pixelVal);

    public int getNrgChnlIndex() {
        return nrgChnlIndex;
    }

    public void setNrgChnlIndex(int nrgChnlIndex) {
        this.nrgChnlIndex = nrgChnlIndex;
    }
}
