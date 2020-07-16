/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.stack.calculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateHistogram extends FeatureCalculation<Histogram, FeatureInputStack> {

    private final int nrgIndex;

    @Override
    protected Histogram execute(FeatureInputStack input) throws FeatureCalcException {
        try {
            return HistogramFactory.create(input.getNrgStackRequired().getChnl(nrgIndex));
        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }
    }
}
