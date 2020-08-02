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

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

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
@EqualsAndHashCode(callSuper = false)
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
