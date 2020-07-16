/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Calculates the threshold value from a quantile of a histogram.
 *
 * <p>By default, it calculates the median i.e. quantile of 0.5
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = false)
public class Quantile extends CalculateLevel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 0.5; // NOSONAR

    @BeanField @Getter @Setter private boolean addOne = false;
    // END BEAN PROPERTIES

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {
        return h.quantile(quantile);
    }
}
