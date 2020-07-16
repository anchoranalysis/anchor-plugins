/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class OverlapRatioMask extends OverlapMaskBase {

    // START BEAN PROPERTIES
    @BeanField private boolean useMax = false;
    // END BEAN PROPERTIES

    private RelationBean relationToThreshold = new EqualToBean();

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        FeatureInputPairMemo inputSessionless = input.get();

        double overlap = overlapWithGlobalMask(input);

        return calcOverlapRatioToggle(
                inputSessionless.getObj1(), inputSessionless.getObj2(), overlap, getRegionID());
    }

    private double calcOverlapRatioToggle(
            VoxelizedMarkMemo obj1, VoxelizedMarkMemo obj2, double overlap, int regionID) {

        if (overlap == 0.0) {
            return 0.0;
        }

        double volume =
                calcVolumeAgg(
                        obj1,
                        obj2,
                        regionID,
                        relationToThreshold,
                        OverlapRatioUtilities.maxOrMin(useMax));
        return overlap / volume;
    }

    public boolean isUseMax() {
        return useMax;
    }

    public void setUseMax(boolean useMax) {
        this.useMax = useMax;
    }
}
