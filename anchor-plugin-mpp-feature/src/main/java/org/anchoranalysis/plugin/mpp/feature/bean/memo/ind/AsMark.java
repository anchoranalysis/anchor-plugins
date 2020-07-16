/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public class AsMark extends FeatureSingleElem<FeatureInputSingleMemo, FeatureInputMark> {

    private static final ChildCacheName CACHE_NAME = new ChildCacheName(AsMark.class);

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {
        return input.forChild().calc(getItem(), new CalculateDeriveMarkFromMemo(), CACHE_NAME);
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
