/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.math.statistics.VarianceCalculator;

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
 * @see Otsu
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OtsuWeighted extends CalculateLevel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double weightForeground = 1.0;

    @BeanField @Getter @Setter private double weightBackground = 1.0;
    // END BEAN PROPERTIES

    @Override
    public int calculateLevel(Histogram histogram) throws OperationFailedException {

        getLogger()
                .messageLogger()
                .logFormatted(
                        "weightForeground=%f weightBackground=%f",
                        weightForeground, weightBackground);

        // Search for max between-class variance
        int minIntensity = histogram.calculateMinimum() + 1;
        int maxIntensity = histogram.calculateMaximum() - 1;

        // If there's only zeroes
        if (maxIntensity == -1) {
            return 0;
        }

        int thresholdChosen = findBestThreshold(histogram, minIntensity, maxIntensity);

        getLogger().messageLogger().logFormatted("chosen threshold=%d", thresholdChosen);

        return thresholdChosen;
    }

    private int findBestThreshold(Histogram histogram, int minIntensity, int maxIntensity) {

        int bestThreshold = 0;

        VarianceCalculator total = varianceCalculatorTotal(histogram);
        VarianceCalculator running = varianceCalculatorFirstBin(histogram, minIntensity - 1);

        double bcvMin = Double.POSITIVE_INFINITY;

        for (int k = minIntensity; k <= maxIntensity; k++) { // Avoid min and max

            running.add(histogram.getCount(k), k);

            double bcv = weightedSumClassVariances(running, total);

            if (bcv <= bcvMin && !Double.isNaN(bcv)) {
                bcvMin = bcv;
                bestThreshold = k;
            }
        }

        return bestThreshold;
    }

    private static VarianceCalculator varianceCalculatorFirstBin(
            Histogram histogram, int firstBin) {
        VarianceCalculator running = new VarianceCalculator(0, 0, 0);
        running.add(histogram.getCount(firstBin), firstBin);
        return running;
    }

    private static VarianceCalculator varianceCalculatorTotal(Histogram histogram) {
        return new VarianceCalculator(
                histogram.calculateSum(),
                histogram.calculateSumSquares(),
                histogram.getTotalCount());
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
}
