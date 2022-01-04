/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.opencv.resizer;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedToDoubleFunction;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.statistics.HistogramFactory;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Compares two histograms, asserting that certain statistics are similar, within certain
 * tolerances.
 *
 * @author Owen Feehan
 */
class CompareHistogramStatistics {

    private static final double TOLERANCE_MEAN = 1.0;

    private static final double TOLERANCE_EXTREMA = 30;

    private static final double TOLERANCE_STD_DEV = 3.0;

    /**
     * Asserts that two {@link VoxelsUntyped} are similar.
     *
     * @param voxels the first voxels to compare.
     * @param voxelsResized the second voxels to compare.
     * @param expectIdenticalValues if true, we expect the distinct values in the output histogram,
     *     to be identical in the input histogram.
     * @throws OperationFailedException
     */
    public static void assertSimilar(
            VoxelsUntyped voxels, VoxelsUntyped voxelsResized, boolean expectIdenticalValues)
            throws OperationFailedException {
        Histogram histogram = HistogramFactory.createFrom(voxels);
        Histogram histogramResized = HistogramFactory.createFrom(voxelsResized);

        assertComparison(histogram, histogramResized, Histogram::mean, TOLERANCE_MEAN);
        assertComparison(
                histogram, histogramResized, Histogram::standardDeviation, TOLERANCE_STD_DEV);
        assertComparison(
                histogram, histogramResized, Histogram::calculateMinimum, TOLERANCE_EXTREMA);
        assertComparison(
                histogram, histogramResized, Histogram::calculateMaximum, TOLERANCE_EXTREMA);

        if (expectIdenticalValues) {
            assertIdenticalDistinctValues(histogram, histogramResized);
        }
    }

    /**
     * Asserts that exactly identical values exist in {@code expected} and {@code actual} (ignoring
     * differences in the corresponding count).
     */
    private static void assertIdenticalDistinctValues(Histogram expected, Histogram actual) {
        assertEquals(valuesInHistogram(expected), valuesInHistogram(actual));
    }

    /** Extracts a set of distinct values in {@code histogram}. */
    private static Set<Integer> valuesInHistogram(Histogram histogram) {
        Set<Integer> values = new HashSet<>();
        histogram.iterateValues(
                (bin, count) -> {
                    if (count > 0) values.add(bin);
                });
        return values;
    }

    /** Checks if a statistic is similar (within a tolerance) between the two histograms. */
    private static <E extends Exception> void assertComparison(
            Histogram expected,
            Histogram actual,
            CheckedToDoubleFunction<Histogram, E> extractStatistic,
            double tolerance)
            throws E {
        assertEquals(
                extractStatistic.applyAsDouble(expected),
                extractStatistic.applyAsDouble(actual),
                tolerance);
    }
}
