/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * The range (difference in values) between two quantiles
 *
 * @author Owen Feehan
 */
public class Range extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField private double quantileLow = 0;

    @BeanField private double quantileHigh = 1.0;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        try {
            double high = histogram.quantile(quantileHigh);
            double low = histogram.quantile(quantileLow);
            return high - low;
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    public double getQuantileLow() {
        return quantileLow;
    }

    public void setQuantileLow(double quantileLow) {
        this.quantileLow = quantileLow;
    }

    public double getQuantileHigh() {
        return quantileHigh;
    }

    public void setQuantileHigh(double quantileHigh) {
        this.quantileHigh = quantileHigh;
    }
}
