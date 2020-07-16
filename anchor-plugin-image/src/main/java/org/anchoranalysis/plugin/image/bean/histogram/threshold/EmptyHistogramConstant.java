/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Specifies a constant if a histogram is empty, otherwise delegates to another {#link
 * org.anchoranalysis.image.bean.threshold.CalculateLevel}
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class EmptyHistogramConstant extends CalculateLevelOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int value = 0;
    // END BEAN PROPERTIES

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {

        if (!h.isEmpty()) {
            return calculateLevelIncoming(h);
        } else {
            return value;
        }
    }
}
