/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateOverlapMask extends FeatureCalculation<Double, FeatureInputPairMemo> {

    private final int regionID;
    private final int nrgIndex;
    private final byte maskOnValue;

    @Override
    protected Double execute(FeatureInputPairMemo input) throws FeatureCalcException {

        VoxelizedMarkMemo mark1 = input.getObj1();
        VoxelizedMarkMemo mark2 = input.getObj2();

        Channel chnl = input.getNrgStackRequired().getNrgStack().getChnl(nrgIndex);

        return OverlapUtilities.overlapWithMaskGlobal(
                mark1, mark2, regionID, chnl.getVoxelBox().asByte(), maskOnValue);
    }
}
