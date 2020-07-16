/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * The maximum value of one or more {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class Maximum extends CalculateLevelListBase {

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {

        int max = -1; // As level should always be >=0
        for (CalculateLevel cl : getList()) {
            int level = cl.calculateLevel(h);
            if (level > max) {
                max = level;
            }
        }
        return max;
    }
}
