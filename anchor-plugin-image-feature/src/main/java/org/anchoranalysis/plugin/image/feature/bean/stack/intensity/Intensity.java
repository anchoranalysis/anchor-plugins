/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.stack.FeatureStack;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.histogram.Mean;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.feature.stack.calculation.CalculateHistogram;
import org.anchoranalysis.plugin.image.feature.stack.calculation.CalculateHistogramMasked;

/**
 * The intensity of a particular channel of the stack, by default the mean-intensity.
 *
 * <p>Alternative statistics to the mean can be calculated via the item bean-field.
 *
 * @author Owen Feehan
 */
public class Intensity extends FeatureStack {

    // START BEAN PROPERTIES
    /** Feature to apply to the histogram */
    @BeanField @Getter @Setter private Feature<FeatureInputHistogram> item = new Mean();

    /** The channel that that forms the histogram */
    @BeanField @Getter @Setter private int nrgIndex = 0;

    /** Optionally, index of another channel that masks the histogram. -1 disables */
    @BeanField @Getter @Setter private int nrgIndexMask = -1;
    // END BEAN PROEPRTIES

    @Override
    protected double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {
        return input.forChild()
                .calc(
                        item,
                        new CalculateDeriveHistogramInput(histogramCalculator(), input.resolver()),
                        new ChildCacheName(Intensity.class, nrgIndex + "_" + nrgIndexMask));
    }

    private FeatureCalculation<Histogram, FeatureInputStack> histogramCalculator() {
        if (nrgIndexMask != -1) {
            return new CalculateHistogramMasked(nrgIndex, nrgIndexMask);
        } else {
            return new CalculateHistogram(nrgIndex);
        }
    }
}
