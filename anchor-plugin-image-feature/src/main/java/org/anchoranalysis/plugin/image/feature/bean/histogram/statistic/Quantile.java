/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

public class Quantile extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 0.0; // NOSONAR
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
        try {
            return histogram.quantile(quantile);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }
}
