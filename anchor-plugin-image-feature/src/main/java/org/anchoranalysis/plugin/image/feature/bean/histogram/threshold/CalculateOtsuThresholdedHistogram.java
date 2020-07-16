/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.bean.init.params.NullInitParams;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.intensity.HistogramThresholder;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateOtsuThresholdedHistogram
        extends FeatureCalculation<Histogram, FeatureInputHistogram> {

    private final CalculateLevel calculateLevel;
    private final Logger logger;

    @Override
    protected Histogram execute(FeatureInputHistogram params) throws FeatureCalcException {
        try {
            if (!calculateLevel.isInitialized()) {
                calculateLevel.init(NullInitParams.instance(), logger);
            }
            return HistogramThresholder.withCalculateLevel(
                    params.getHistogram().duplicate(), // Important to duplicate
                    calculateLevel);
        } catch (OperationFailedException | InitException e) {
            throw new FeatureCalcException(e);
        }
    }
}
