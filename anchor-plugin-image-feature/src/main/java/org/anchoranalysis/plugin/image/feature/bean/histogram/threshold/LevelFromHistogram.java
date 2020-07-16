/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;

/**
 * Calculates a level (a threshold-value) from a histogram.
 *
 * @author Owen Feehan
 */
public class LevelFromHistogram extends FeatureHistogram {

    // START BEAN PROPERTIES
    @BeanField private CalculateLevel calculateLevel;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {
        try {
            return calculateLevel.calculateLevel(input.get().getHistogram());
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    public CalculateLevel getCalculateLevel() {
        return calculateLevel;
    }

    public void setCalculateLevel(CalculateLevel calculateLevel) {
        this.calculateLevel = calculateLevel;
    }
}
