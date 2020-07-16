/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Performs Otsu auto-thresholding
 *
 * <p>This performs binary thresholding into foreground and background.
 *
 * <p>This minimizes intra-class intensity variance, or equivalently, maximizes inter-class
 * variance. <@see <a href="https://en.wikipedia.org/wiki/Otsu%27s_method>Otsu's method on
 * wikipedia</a>.
 *
 * @author Owen Feehan
 */
public class Otsu extends CalculateLevel {

    @Override
    public int calculateLevel(Histogram hist) throws OperationFailedException {

        long totalSum = hist.calcSum();
        long totalCount = hist.getTotalCount();

        long runningSum = 0;
        long runningCount = hist.getCount(0);

        double bcvMax = Double.NEGATIVE_INFINITY;
        int thresholdChosen = 0;

        // Search for max between-class variance
        int minIntensity = hist.calcMin() + 1;
        int maxIntensity = hist.calcMax() - 1;
        for (int k = minIntensity; k <= maxIntensity; k++) { // Avoid min and max
            runningSum += k * hist.getCount(k);
            runningCount += hist.getCount(k);

            double bcv = betweenClassVariance(runningSum, runningCount, totalSum, totalCount);

            if (bcv >= bcvMax && !Double.isNaN(bcv)) {
                bcvMax = bcv;
                thresholdChosen = k;
            }
        }

        return thresholdChosen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Otsu) {
            return new EqualsBuilder().isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().toHashCode();
    }

    private static double betweenClassVariance(
            long runningSum, long runningCount, long totalSum, long totalCount) {

        double denom = ((double) runningCount) * (totalCount - runningCount);

        if (denom == 0) {
            return Double.NaN;
        }

        double num = ((double) runningCount / totalCount) * totalSum - runningSum;
        return (num * num) / denom;
    }
}
