/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.Divide;

/**
 * Calculates the aggregate of the ratio of <code>first</code>:<code>second</code> and <code>second
 * </code>:<code>first</code> for a {@link FeatureInputPairObjects}
 *
 * <p><code>first</code> and <code>second</code> refer respectively to a feature calculated on the
 * first and second objects of a {@link FeatureInputPairObjects}.
 *
 * @author Owen Feehan
 */
public class ReduceRatioFirstAndSecond extends FeatureListProviderAggregatePair {

    @Override
    protected Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged) {
        return createReducedFeature(
                ratioTwoFeatures(first, second), ratioTwoFeatures(second, first) // NOSONAR
                );
    }

    private static <T extends FeatureInput> Feature<T> ratioTwoFeatures(
            Feature<T> first, Feature<T> second) {
        Divide<T> out = new Divide<>();
        out.setAvoidDivideByZero(true);
        out.getList().add(first);
        out.getList().add(second);
        return out;
    }
}
