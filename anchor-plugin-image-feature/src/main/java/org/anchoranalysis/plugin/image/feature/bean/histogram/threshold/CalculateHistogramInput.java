/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegate;

@EqualsAndHashCode(callSuper = true)
class CalculateHistogramInput
        extends CalculateInputFromDelegate<
                FeatureInputHistogram, FeatureInputHistogram, Histogram> {

    public CalculateHistogramInput(
            ResolvedCalculation<Histogram, FeatureInputHistogram> ccDelegate) {
        super(ccDelegate);
    }

    public CalculateHistogramInput(
            FeatureCalculation<Histogram, FeatureInputHistogram> ccDelegate,
            CalculationResolver<FeatureInputHistogram> cache) {
        super(ccDelegate, cache);
    }

    @Override
    protected FeatureInputHistogram deriveFromDelegate(
            FeatureInputHistogram params, Histogram delegate) {
        return new FeatureInputHistogram(delegate, params.getResOptional());
    }
}
