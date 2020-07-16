/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Clips a value if it lies outside the range - instead returning particular constants if below or
 * above an acceptable range
 *
 * @author Owen Feehan
 * @param <T>
 */
public class IfOutsideRange<T extends FeatureInput> extends RangeCompareFromScalars<T> {

    @Override
    protected Feature<T> featureToCalcInputVal() {
        return getItem();
    }

    @Override
    protected double withinRangeValue(double valWithinRange, SessionInput<T> input)
            throws FeatureCalcException {
        return valWithinRange;
    }
}
