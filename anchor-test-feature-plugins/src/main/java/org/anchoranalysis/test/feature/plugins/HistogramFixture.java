/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.histogram.Histogram;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistogramFixture {

    private static final int MAX_VAL = 255;

    private static Histogram createEmpty() {
        return new Histogram(MAX_VAL + 1);
    }

    /**
     * a histogram from 0 to 255 (inclusive) where each bin is equal to its index (value 5 has count
     * 5, value 100 has count 100 etc.)
     */
    public static Histogram createAscending() {

        Histogram histogram = createEmpty();
        for (int i = 0; i <= MAX_VAL; i++) {
            histogram.incrementValueBy(i, i);
        }
        return histogram;
    }

    public static Histogram createDescending() {

        Histogram histogram = createEmpty();
        for (int i = 0; i <= MAX_VAL; i++) {
            histogram.incrementValueBy(i, MAX_VAL - i);
        }
        return histogram;
    }
}
