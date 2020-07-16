/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.Sum;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;

/**
 * Calculates <code>merged - reduce(first,second)</code> for a {@link FeatureInputPairObjects}
 *
 * <p><code>first</code>, <code>second</code> and <code>merged</code> refer respectively to a
 * feature calculated on the first, second and merged objects of a {@link FeatureInputPairObjects}.
 *
 * @author Owen Feehan
 */
public class SubtractReducedFromMerged extends FeatureListProviderAggregatePair {

    @Override
    protected Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged) {
        return createSum(merged, createReducedFeature(first, second));
    }

    private static Feature<FeatureInputPairObjects> createSum(
            Feature<FeatureInputPairObjects> featMerged,
            Feature<FeatureInputPairObjects> featWithList) {
        Sum<FeatureInputPairObjects> featSum = new Sum<>();
        featSum.getList().add(featMerged);
        featSum.getList().add(new MultiplyByConstant<>(featWithList, -1));
        return featSum;
    }
}
