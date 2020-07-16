/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Counts the number of items in a histogram (posssibly in relation to a threshold: above, below
 * etc.)
 *
 * @author Owen Feehan
 */
public class Count extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) {
        if (threshold != null) {
            return histogram.countThreshold(threshold);
        } else {
            return histogram.getTotalCount();
        }
    }

    @Override
    public String getParamDscr() {
        return String.format(
                "%f,%s,threshold=%s", threshold, super.getParamDscr(), threshold.toString());
    }

    public RelationToThreshold getThreshold() {
        return threshold;
    }

    public void setThreshold(RelationToThreshold threshold) {
        this.threshold = threshold;
    }
}
