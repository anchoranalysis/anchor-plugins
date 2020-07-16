/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreDifference extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int nrgChnlIndexFirst = 0;

    @BeanField @Getter @Setter private int nrgChnlIndexSecond = 0;

    @BeanField @Getter @Setter private double width = 10;

    @BeanField @Getter @Setter private int minDifference = 0;
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
}
