/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

/**
 * Expresses the number of intersecting pixels between two objects as a ratio to something else
 * (denominator)
 *
 * @author Owen Feehan
 */
public abstract class OverlapRelative extends FeaturePairObjects {

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {

        FeatureInputPairObjects inputSessionless = input.get();

        return OverlapRatioUtilities.overlapRatioTo(
                inputSessionless, () -> calcDenominator(inputSessionless));
    }

    protected abstract int calcDenominator(FeatureInputPairObjects params);
}
