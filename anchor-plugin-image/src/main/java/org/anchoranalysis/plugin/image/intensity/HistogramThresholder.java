/* (C)2020 */
package org.anchoranalysis.plugin.image.intensity;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

public class HistogramThresholder {

    private HistogramThresholder() {}

    /** Retains the portion of the histogram greater or equal to the calculated-level */
    public static Histogram withCalculateLevel(Histogram h, CalculateLevel calculateLevel)
            throws OperationFailedException {
        int threshold = calculateLevel.calculateLevel(h);
        h.removeBelowThreshold(threshold);
        return h;
    }
}
