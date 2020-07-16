/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreDifference extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private int nrgChnlIndexFirst = 0;

    @BeanField private int nrgChnlIndexSecond = 0;

    @BeanField private double width = 10;

    @BeanField private int minDifference = 0;
    // END BEAN PROPERTIES

    public static double calcDiffFromValue(
            int valFirst,
            int valSecond,
            double widthGreaterThan,
            double widthLessThan,
            int minDifference) {
        double diff = (double) (valFirst - valSecond - minDifference);

        if (diff < (-1 * widthLessThan)) {
            return 0.0;
        }

        if (diff > widthGreaterThan) {
            return 1.0;
        }

        if (diff < 0) {
            return (diff / (widthLessThan * 2)) + 0.5;
        } else {
            return (diff / (widthGreaterThan * 2)) + 0.5;
        }
    }

    public static double calcDiffFromValue(
            int valFirst, int valSecond, double width, int minDifference) {
        return calcDiffFromValue(valFirst, valSecond, width, width, minDifference);
    }

    public static double calcDiffFromParams(
            int[] pixelVals,
            int nrgChnlIndexFirst,
            int nrgChnlIndexSecond,
            double width,
            int minDifference) {
        return calcDiffFromValue(
                pixelVals[nrgChnlIndexFirst], pixelVals[nrgChnlIndexSecond], width, minDifference);
    }

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {
        return calcDiffFromParams(
                pixelVals, nrgChnlIndexFirst, nrgChnlIndexSecond, width, minDifference);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public int getNrgChnlIndexFirst() {
        return nrgChnlIndexFirst;
    }

    public void setNrgChnlIndexFirst(int nrgChnlIndexFirst) {
        this.nrgChnlIndexFirst = nrgChnlIndexFirst;
    }

    public int getNrgChnlIndexSecond() {
        return nrgChnlIndexSecond;
    }

    public void setNrgChnlIndexSecond(int nrgChnlIndexSecond) {
        this.nrgChnlIndexSecond = nrgChnlIndexSecond;
    }

    public int getMinDifference() {
        return minDifference;
    }

    public void setMinDifference(int minDifference) {
        this.minDifference = minDifference;
    }
}
