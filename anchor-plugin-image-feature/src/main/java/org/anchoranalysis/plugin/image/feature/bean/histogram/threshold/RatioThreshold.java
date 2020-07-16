/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

public class RatioThreshold extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        if (histogram.size() == 0) {
            return 0.0;
        }

        assert (histogram.size() > 0);

        long count = histogram.countThreshold(threshold);
        return (((double) count) / histogram.size());
    }

    @Override
    public String getParamDscr() {
        return String.format("%s,threshold=%s", super.getParamDscr(), threshold.toString());
    }

    public RelationToThreshold getThreshold() {
        return threshold;
    }

    public void setThreshold(RelationToThreshold threshold) {
        this.threshold = threshold;
    }
}
