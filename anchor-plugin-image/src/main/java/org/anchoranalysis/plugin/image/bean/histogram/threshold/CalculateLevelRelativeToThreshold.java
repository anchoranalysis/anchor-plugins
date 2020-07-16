/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

@EqualsAndHashCode(callSuper = true)
public abstract class CalculateLevelRelativeToThreshold extends CalculateLevelOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CalculateLevel calculateLevelElse;

    @BeanField @Getter @Setter private int threshold;
    // END BEAN PROPERTIES

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {

        int level = calculateLevelIncoming(h);
        if (useElseInstead(level, threshold)) {
            return calculateLevelElse.calculateLevel(h);
        } else {
            return level;
        }
    }

    /** Uses the {@link calculateLevelElse} instead of {@link calculateLevel} */
    protected abstract boolean useElseInstead(int level, int threshold);
}
