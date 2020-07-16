/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateDeriveMarkFromMemo
        extends FeatureCalculation<FeatureInputMark, FeatureInputSingleMemo> {

    @Override
    protected FeatureInputMark execute(FeatureInputSingleMemo input) {
        return new FeatureInputMark(
                input.getPxlPartMemo().getMark(), input.getDimensionsOptional());
    }
}
