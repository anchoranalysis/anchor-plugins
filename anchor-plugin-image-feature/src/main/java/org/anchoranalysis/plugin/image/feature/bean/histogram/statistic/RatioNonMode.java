/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

//
// Ratio of number of non-mode pixels to number of pixels
//
public class RatioNonMode extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField private boolean ignoreZero = false;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        try {
            int startV = ignoreZero ? 1 : 0;

            int mode = findMode(histogram, startV);

            // Calculate number of non-modal
            int totalCnt = 0;
            int nonModalCnt = 0;

            for (int v = startV; v < 255; v++) {

                RelationToThreshold relation = new RelationToConstant(new EqualToBean(), v);

                long cnt = histogram.countThreshold(relation);

                if (cnt != 0) {
                    if (v != mode) {
                        nonModalCnt += cnt;
                    }
                    totalCnt += cnt;
                }
            }

            if (totalCnt == 0) {
                return Double.POSITIVE_INFINITY;
            }

            return ((double) nonModalCnt) / totalCnt;
        } catch (IndexOutOfBoundsException e) {
            throw new FeatureCalcException(e);
        }
    }

    private int findMode(Histogram histogram, int startV) {

        // Find mode
        int maxIndex = -1;
        long maxValue = -1;
        for (int v = startV; v < 255; v++) {

            RelationToThreshold relation = new RelationToConstant(new EqualToBean(), v);

            long cnt = histogram.countThreshold(relation);

            if (cnt > maxValue) {
                maxValue = cnt;
                maxIndex = v;
            }
        }
        return maxIndex;
    }

    public boolean isIgnoreZero() {
        return ignoreZero;
    }

    public void setIgnoreZero(boolean ignoreZero) {
        this.ignoreZero = ignoreZero;
    }
}
