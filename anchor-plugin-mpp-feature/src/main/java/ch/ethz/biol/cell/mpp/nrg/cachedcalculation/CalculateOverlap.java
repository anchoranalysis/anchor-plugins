/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateOverlap extends FeatureCalculation<Double, FeatureInputPairMemo> {

    private final int regionID;

    @Override
    protected Double execute(FeatureInputPairMemo params) {
        return OverlapUtilities.overlapWith(params.getObj1(), params.getObj2(), regionID);
    }
}
