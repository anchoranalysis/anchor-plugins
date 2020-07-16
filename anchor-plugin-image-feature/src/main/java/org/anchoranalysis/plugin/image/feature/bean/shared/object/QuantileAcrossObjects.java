/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.input.FeatureInputNRG;

/**
 * Calculates the quantile of a feature applied to each connected component
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public class QuantileAcrossObjects<T extends FeatureInputNRG> extends ObjectAggregationBase<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 0.5;
    // END BEAN PROPERTIES

    @Override
    protected double deriveStatistic(DoubleArrayList featureVals) {
        featureVals.sort();
        return Descriptive.quantile(featureVals, quantile);
    }
}
