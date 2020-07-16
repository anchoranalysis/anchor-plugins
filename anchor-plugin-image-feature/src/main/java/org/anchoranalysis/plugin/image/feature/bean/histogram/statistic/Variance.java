/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

public class Variance extends FeatureHistogramStatistic {

    @Override
    protected double calcStatisticFrom(Histogram histogram) {
        return histogram.variance();
    }
}
