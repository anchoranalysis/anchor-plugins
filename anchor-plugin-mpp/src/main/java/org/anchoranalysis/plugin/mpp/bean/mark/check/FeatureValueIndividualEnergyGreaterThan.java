/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemoFactory;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class FeatureValueIndividualEnergyGreaterThan
        extends FeatureValueCheckMark<FeatureInputSingleMemo> {

    @Override
    protected FeatureInputSingleMemo createFeatureCalcParams(
            Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {
        VoxelizedMarkMemo pmm = PxlMarkMemoFactory.create(mark, nrgStack.getNrgStack(), regionMap);
        return new FeatureInputSingleMemo(pmm, Optional.of(nrgStack));
    }
}
