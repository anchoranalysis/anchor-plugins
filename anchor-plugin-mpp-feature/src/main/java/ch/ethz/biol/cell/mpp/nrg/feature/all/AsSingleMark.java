/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class AsSingleMark extends FeatureSingleElem<FeatureInputAllMemo, FeatureInputMark> {

    private static final ChildCacheName CACHE_NAME = new ChildCacheName(AsSingleMark.class);

    @Override
    public double calc(SessionInput<FeatureInputAllMemo> input) throws FeatureCalcException {

        return input.forChild().calc(getItem(), new CalculateDeriveMarkInput(), CACHE_NAME);
    }
}
