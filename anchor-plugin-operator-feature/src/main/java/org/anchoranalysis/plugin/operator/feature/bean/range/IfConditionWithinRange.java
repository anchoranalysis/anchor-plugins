/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Calculates a value if a condition lies within a certain range, otherwise returns constants
 *
 * @author Owen Feehan
 * @param <T> feature input type
 */
public class IfConditionWithinRange<T extends FeatureInput> extends RangeCompareFromScalars<T> {

    // START BEAN PROPERTIES
    /** Calculates value for the condition - which is checked if it lies within a certain range */
    @BeanField private Feature<T> featureCondition;
    // END BEAN PROPERTIES

    @Override
    protected Feature<T> featureToCalcInputVal() {
        return featureCondition;
    }

    @Override
    protected double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalcException {
        // If the condition lies within the range, then we calculate the derived feature as intended
        return input.calc(getItem());
    }

    public Feature<T> getFeatureCondition() {
        return featureCondition;
    }

    public void setFeatureCondition(Feature<T> featureCondition) {
        this.featureCondition = featureCondition;
    }
}
