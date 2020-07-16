/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class AsObject extends FeatureSingleElem<FeatureInputSingleMemo, FeatureInputSingleObject> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private int index = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {
        return input.forChild()
                .calc(
                        getItem(),
                        new CalculateSingleObjFromMemo(regionMap, index),
                        new ChildCacheName(AsObject.class, index));
    }

    // We change the default behaviour, as we don't want to give the same paramsFactory
    //   as the item we pass to
    @Override
    public Class<? extends FeatureInput> inputType() {
        return FeatureInputSingleMemo.class;
    }

    @Override
    public String getParamDscr() {
        return getItem().getParamDscr();
    }
}
