/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.math.statistics.VarianceCalculator;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Similar to Otsu, but weighs the variances differently of background and foreground
 *
 * <p>The standard otsu method seeks both foreground and background to have similar variance. But
 * many for real world cases (e.g. fluorescent microscopy) one expects the background to be have
 * much less variance, as it is very low-valued.
 *
 * <p>This algorithm allows the foreground and background variance to be non-equally weighted to a
 * priori account for low-variance backgrounds.
 *
 * @see {#link org.anchoranalysis.image.bean.threshold.calculatelevel.Otsu}
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class OtsuWeighted extends CalculateLevel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double weightForeground = 1.0;

    @BeanField @Getter @Setter private double weightBackground = 1.0;
    // END BEAN PROPERTIES

    @Override
    public int calculateLevel(Histogram hist) throws OperationFailedException {

        getLogger()
                .messageLogger()
                .logFormatted(
                        "weightForeground=%f weightBackground=%f",
                        weightForeground, weightBackground);

        // Search for max between-class variance
        int minIntensity = hist.calcMin() + 1;
        int maxIntensity = hist.calcMax() - 1;

        // If there's only zeroes
        if (maxIntensity == -1) {
            return 0;
        }

        int thresholdChosen = findBestThreshold(hist, minIntensity, maxIntensity);

        getLogger().messageLogger().logFormatted("chosen threshold=%d", thresholdChosen);

        return thresholdChosen;
    }

    private int findBestThreshold(Histogram hist, int minIntensity, int maxIntensity) {

        int bestThreshold = 0;

        VarianceCalculator total = varianceCalculatorTotal(hist);
        VarianceCalculator running = varianceCalculatorFirstBin(hist, minIntensity - 1);

        double bcvMin = Double.POSITIVE_INFINITY;

        for (int k = minIntensity; k <= maxIntensity; k++) { // Avoid min and max

            running.add(hist.getCount(k), k);

            double bcv = weightedSumClassVariances(running, total);

            if (bcv <= bcvMin && !Double.isNaN(bcv)) {
                bcvMin = bcv;
                bestThreshold = k;
            }
        }

        return bestThreshold;
    }

    private static VarianceCalculator varianceCalculatorFirstBin(Histogram hist, int firstBin) {
        VarianceCalculator running = new VarianceCalculator(0, 0, 0);
        running.add(hist.getCount(firstBin), firstBin);
        return running;
    }

    private static VarianceCalculator varianceCalculatorTotal(Histogram hist) {
        return new VarianceCalculator(hist.calcSum(), hist.calcSumSquares(), hist.getTotalCount());
    }

    private double weightedSumClassVariances(VarianceCalculator running, VarianceCalculator total) {
        assert (total.greaterEqualThan(running));

        double varBg = running.variance();

        VarianceCalculator sub = total.subtract(running);
        double varFg = sub.variance();

        double prob1 = ((double) running.getCount()) / total.getCount();
        double prob2 = 1 - prob1;

        return (prob1 * varBg * weightBackground + prob2 * varFg * weightForeground);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OtsuWeighted) {
            final OtsuWeighted other = (OtsuWeighted) obj;
            return new EqualsBuilder()
                    .append(weightForeground, other.weightForeground)
                    .append(weightBackground, other.weightBackground)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(weightForeground).append(weightBackground).toHashCode();
    }
}
