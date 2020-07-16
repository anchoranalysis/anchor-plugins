/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateDeriveSingleInputFromPair
        extends FeatureCalculation<FeatureInputSingleMemo, FeatureInputPairMemo> {

    /** Iff TRUE, then the first object from the pair is used, otherwise the second is */
    private final boolean first;

    @Override
    protected FeatureInputSingleMemo execute(FeatureInputPairMemo input) {
        return new FeatureInputSingleMemo(
                first ? input.getObj1() : input.getObj2(), input.getNrgStackOptional());
    }
}
