/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MinFeatureAsIndividual extends FeaturePairMemoOne {

    private static final ChildCacheName CACHE_NAME_FIRST =
            new ChildCacheName(MinFeatureAsIndividual.class, "first");
    private static final ChildCacheName CACHE_NAME_SECOND =
            new ChildCacheName(MinFeatureAsIndividual.class, "second");

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        return Math.min(calcForInd(input, true), calcForInd(input, false));
    }

    private double calcForInd(SessionInput<FeatureInputPairMemo> input, boolean first)
            throws FeatureCalcException {

        return input.forChild()
                .calc(
                        getItem(),
                        new CalculateDeriveSingleInputFromPair(first),
                        first ? CACHE_NAME_FIRST : CACHE_NAME_SECOND);
    }
}
