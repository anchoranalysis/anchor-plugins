/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

// Number of unique values in histogram i.e. how many non-zero bins
public class NumberDistinctValues extends FeatureHistogramStatistic {

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        int numUniqueValues = 0;

        for (int v = 0; v < 255; v++) {
            long cnt = histogram.countThreshold(new RelationToConstant(new EqualToBean(), v));

            if (cnt != 0) {
                numUniqueValues++;
            }
        }

        return numUniqueValues;
    }
}
