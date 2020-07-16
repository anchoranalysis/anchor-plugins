/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * The minimum value of one or more {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class Minimum extends CalculateLevelListBase {

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {

        int min = -1; // We will always be greater as calculateLevel returns >=0
        for (CalculateLevel cl : getList()) {
            int level = cl.calculateLevel(h);
            if (level < min || min == -1) {
                min = level;
            }
        }
        return min;
    }
}
