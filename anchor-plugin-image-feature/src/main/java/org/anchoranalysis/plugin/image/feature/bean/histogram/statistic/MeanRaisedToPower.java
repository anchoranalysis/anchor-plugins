/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

/*** The mean of the intensity raised to power */
public class MeanRaisedToPower extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    private double power = 1.0;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        try {
            return histogram.mean(power);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
