/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

/**
 * Calculates the aggregate of a feature applied to both the first and second objects in a {@link
 * FeatureInputPairObjects}.
 *
 * @author Owen Feehan
 */
public class ReduceFirstAndSecond extends FeatureListProviderAggregatePair {

    @Override
    protected Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged) {
        return createReducedFeature(first, second);
    }
}
