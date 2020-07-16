/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.CalculateOverlap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

public class MaxOverlapRatio extends FeaturePairMemoSingleRegion {

    // START BEAN PROPERTIES
    @BeanField private double max = -1;

    @BeanField private double penaltyValue = -10;

    @BeanField private boolean includeShell = false;
    // END BEAN PROPERTIES

    public MaxOverlapRatio() {}

    public MaxOverlapRatio(double maxOverlap) {
        this();
        this.max = maxOverlap;
    }

    @Override
    public String getParamDscr() {
        return String.format("max=%f", max);
    }

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        FeatureInputPairMemo params = input.get();

        double ratio =
                OverlapRatioUtilities.calcOverlapRatio(
                        params.getObj1(),
                        params.getObj2(),
                        input.calc(new CalculateOverlap(getRegionID())),
                        getRegionID(),
                        false,
                        Math::min);

        if (ratio > max) {
            return penaltyValue;
        } else {
            return 0;
        }
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getPenaltyValue() {
        return penaltyValue;
    }

    public void setPenaltyValue(double penaltyValue) {
        this.penaltyValue = penaltyValue;
    }

    public boolean isIncludeShell() {
        return includeShell;
    }

    public void setIncludeShell(boolean includeShell) {
        this.includeShell = includeShell;
    }
}
