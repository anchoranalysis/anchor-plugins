/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.plugin.operator.feature.score.GaussianScoreCalculator;

public class PixelScoreGaussianKeyValueParams extends PixelScoreParamsBase {

    // START BEAN PROPERTIES
    @BeanField private String keyMean;

    @BeanField private String keyStdDev;

    @BeanField private double shift;
    // END BEAN PROPERTIES

    private double mean;
    private double stdDev;

    @Override
    protected void setupParams(KeyValueParams kpv) throws InitException {
        mean = extractParamsAsDouble(kpv, keyMean);
        stdDev = extractParamsAsDouble(kpv, keyStdDev);
    }

    @Override
    protected double deriveScoreFromPixelVal(int pixelVal) {

        // Values higher than the mean should be included for definite
        if (pixelVal > mean) {
            return 1.0;
        }

        double scoreBeforeShift =
                GaussianScoreCalculator.calc(mean, stdDev, pixelVal, false, false);

        double scoreShifted = (scoreBeforeShift - shift) / (1 - shift);

        return (scoreShifted / 2) + 0.5;
    }

    public String getKeyMean() {
        return keyMean;
    }

    public void setKeyMean(String keyMean) {
        this.keyMean = keyMean;
    }

    public String getKeyStdDev() {
        return keyStdDev;
    }

    public void setKeyStdDev(String keyStdDev) {
        this.keyStdDev = keyStdDev;
    }

    public double getShift() {
        return shift;
    }

    public void setShift(double shift) {
        this.shift = shift;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }
}
