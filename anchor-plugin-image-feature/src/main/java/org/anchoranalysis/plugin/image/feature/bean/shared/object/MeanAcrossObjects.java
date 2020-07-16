/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import org.anchoranalysis.feature.input.FeatureInputNRG;

/**
 * Calculates the mean of a feature applied to each connected component
 *
 * @param <T> feature-input
 */
public class MeanAcrossObjects<T extends FeatureInputNRG> extends ObjectAggregationBase<T> {

    @Override
    protected double deriveStatistic(DoubleArrayList featureVals) {
        return Descriptive.mean(featureVals);
    }
}
