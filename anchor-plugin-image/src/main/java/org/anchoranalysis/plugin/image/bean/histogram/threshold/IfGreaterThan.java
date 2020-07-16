/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;

/**
 * Calculates a level from the first delegate and if greater than a threshold, recalculates from a
 * second delegate.
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class IfGreaterThan extends CalculateLevelRelativeToThreshold {

    @Override
    protected boolean useElseInstead(int level, int threshold) {
        return level > threshold;
    }
}
