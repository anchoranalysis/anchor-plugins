/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Calculates the ratio of non-mode pixels to total pixels in a {@link Histogram}.
 *
 * <p>The mode is the most frequent value in the histogram.
 */
public class RatioNonMode extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    /**
     * Whether to ignore the zero value when calculating the ratio.
     *
     * <p>If true, pixels with value 0 are not considered in the calculation.
     */
    @BeanField @Getter @Setter private boolean ignoreZero = false;
    // END BEAN PROPERTIES

    @Override
    protected double calculateStatisticFrom(Histogram histogram)
            throws FeatureCalculationException {
        try {
            int startV = ignoreZero ? 1 : 0;

            int mode = findMode(histogram, startV);

            // Calculate number of non-modal
            int totalCount = 0;
            int nonModalCount = 0;

            for (int value = startV; value < 255; value++) {
                long count = histogram.getCount(value);

                if (count != 0) {
                    if (value != mode) {
                        nonModalCount += count;
                    }
                    totalCount += count;
                }
            }

            if (totalCount == 0) {
                return Double.POSITIVE_INFINITY;
            }

            return ((double) nonModalCount) / totalCount;
        } catch (IndexOutOfBoundsException e) {
            throw new FeatureCalculationException(e);
        }
    }

    /**
     * Finds the mode (most frequent value) in the histogram.
     *
     * @param histogram the {@link Histogram} to analyze
     * @param startValue the starting value to consider (usually 0 or 1)
     * @return the mode value
     */
    private int findMode(Histogram histogram, int startValue) {
        int maxIndex = -1;
        long maxValue = -1;
        for (int value = startValue; value < 255; value++) {
            long count = histogram.getCount(value);
            if (count > maxValue) {
                maxValue = count;
                maxIndex = value;
            }
        }
        return maxIndex;
    }
}
