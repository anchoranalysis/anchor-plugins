/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class OverlapRatio extends OverlapMIPBase {

    // START BEAN PROPERTIES
    @BeanField private boolean useMax = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        FeatureInputPairMemo inputSessionless = input.get();

        return OverlapRatioUtilities.calcOverlapRatio(
                inputSessionless.getObj1(),
                inputSessionless.getObj2(),
                overlappingNumVoxels(input),
                getRegionID(),
                isMip(),
                OverlapRatioUtilities.maxOrMin(useMax));
    }

    public boolean isUseMax() {
        return useMax;
    }

    public void setUseMax(boolean useMax) {
        this.useMax = useMax;
    }
}
