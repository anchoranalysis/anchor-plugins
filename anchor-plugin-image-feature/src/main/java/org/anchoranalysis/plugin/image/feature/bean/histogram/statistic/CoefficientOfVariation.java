/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramStatistics;

public class CoefficientOfVariation extends FeatureHistogramStatistic {

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        try {
            return HistogramStatistics.coefficientOfVariation(histogram);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }
}
