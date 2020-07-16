/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.MarkRegion;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.histogram.Mean;

public class Intensity extends FeatureSingleMemo {

    // START BEAN
    /** Feature to apply to the histogram */
    @BeanField private Feature<FeatureInputHistogram> item = new Mean();

    /** If TRUE, zeros are excluded from considering in the histogram */
    @BeanField private boolean excludeZero = false;

    @BeanField private MarkRegion region;
    // END BEAN

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        return input.forChild()
                .calc(item, new CalculateHistogramInputFromMemo(region, excludeZero), cacheName());
    }

    private ChildCacheName cacheName() {
        return new ChildCacheName(Intensity.class, region.uniqueName());
    }

    @Override
    public String getParamDscr() {
        return region.getBeanDscr();
    }

    public boolean isExcludeZero() {
        return excludeZero;
    }

    public void setExcludeZero(boolean excludeZero) {
        this.excludeZero = excludeZero;
    }

    public MarkRegion getRegion() {
        return region;
    }

    public void setRegion(MarkRegion region) {
        this.region = region;
    }

    public Feature<FeatureInputHistogram> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputHistogram> item) {
        this.item = item;
    }
}
