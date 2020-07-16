/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateDeriveSingleMemoInput
        extends FeatureCalculation<FeatureInputSingleMemo, FeatureInputAllMemo> {

    private final int index;

    @Override
    protected FeatureInputSingleMemo execute(FeatureInputAllMemo input) {
        FeatureInputSingleMemo paramsInd =
                new FeatureInputSingleMemo(null, input.getNrgStackOptional());
        paramsInd.setPxlPartMemo(input.getPxlPartMemo().getMemoForIndex(index));
        return paramsInd;
    }
}
