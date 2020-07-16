/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;

public abstract class FeaturePairMemoOne extends FeaturePairMemo {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputSingleMemo> item;
    // END BEAN PROPERTIES

    public Feature<FeatureInputSingleMemo> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputSingleMemo> item) {
        this.item = item;
    }
}
