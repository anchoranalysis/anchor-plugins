/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MeanIntensityDifference extends FeatureSingleMemo {

    // START BEAN PROPERTIES
    @BeanField private double minDiff;
    // END BEAN PROPERTIES

    public MeanIntensityDifference() {
        super();
    }

    public MeanIntensityDifference(double minDiff) {
        super();
        this.minDiff = minDiff;
    }

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> params) throws FeatureCalcException {

        VoxelizedMark pm = params.get().getPxlPartMemo().voxelized();

        double mean_in =
                pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_INSIDE).mean();
        double mean_shell =
                pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_SHELL).mean();

        return ((mean_in - mean_shell) - minDiff) / 255;
    }

    @Override
    public String getParamDscr() {
        return String.format("minDiff=%f", minDiff);
    }

    public double getMinDiff() {
        return minDiff;
    }

    public void setMinDiff(double minDiff) {
        this.minDiff = minDiff;
    }
}
