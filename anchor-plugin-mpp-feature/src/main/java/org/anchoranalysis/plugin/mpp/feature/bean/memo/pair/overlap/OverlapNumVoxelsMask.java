/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class OverlapNumVoxelsMask extends OverlapMaskBase {

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> params) throws FeatureCalcException {
        return overlapWithGlobalMask(params);
    }
}
