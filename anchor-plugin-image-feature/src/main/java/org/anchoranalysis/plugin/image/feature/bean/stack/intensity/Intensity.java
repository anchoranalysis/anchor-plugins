/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.bean.stack.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
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
    @BeanField @Getter @Setter private int energyIndex = 0;

    /** Optionally, index of another channel that masks the histogram. -1 disables */
    @BeanField @Getter @Setter private int energyIndexMask = -1;
    // END BEAN PROEPRTIES

    @Override
    protected double calculate(SessionInput<FeatureInputStack> input)
            throws FeatureCalculationException {
        return input.forChild()
                .calculate(
                        item,
                        new CalculateDeriveHistogramInput(histogramCalculator(), input.resolver()),
                        new ChildCacheName(Intensity.class, energyIndex + "_" + energyIndexMask));
    }

    private FeatureCalculation<Histogram, FeatureInputStack> histogramCalculator() {
        if (energyIndexMask != -1) {
            return new CalculateHistogramMasked(energyIndex, energyIndexMask);
        } else {
            return new CalculateHistogram(energyIndex);
        }
    }
}
