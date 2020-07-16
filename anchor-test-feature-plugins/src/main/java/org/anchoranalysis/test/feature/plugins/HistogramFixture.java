/* (C)2020 */
package org.anchoranalysis.test.feature.plugins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistogramFixture {

    private static final int MAX_VAL = 255;

    private static Histogram createEmpty() {
        return new HistogramArray(MAX_VAL + 1);
    }

    /**
     * a histogram from 0 to 255 (inclusive) where each bin is equal to its index (value 5 has count
     * 5, value 100 has count 100 etc.)
     */
    public static Histogram createAscending() {

        Histogram h = createEmpty();
        for (int i = 0; i <= MAX_VAL; i++) {
            h.incrValBy(i, i);
        }
        return h;
    }

    public static Histogram createDescending() {

        Histogram h = createEmpty();
        for (int i = 0; i <= MAX_VAL; i++) {
            h.incrValBy(i, MAX_VAL - i);
        }
        return h;
    }
}
