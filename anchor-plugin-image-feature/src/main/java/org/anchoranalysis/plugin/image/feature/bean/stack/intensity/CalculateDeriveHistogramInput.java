/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.intensity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateDeriveHistogramInput
        extends FeatureCalculation<FeatureInputHistogram, FeatureInputStack> {

    private final ResolvedCalculation<Histogram, FeatureInputStack> histogramCalculation;

    public CalculateDeriveHistogramInput(
            FeatureCalculation<Histogram, FeatureInputStack> histogramCalculation,
            CalculationResolver<FeatureInputStack> resolver) {
        this(resolver.search(histogramCalculation));
    }

    @Override
    protected FeatureInputHistogram execute(FeatureInputStack input) throws FeatureCalcException {
        return new FeatureInputHistogram(
                histogramCalculation.getOrCalculate(input), input.getResOptional());
    }
}
