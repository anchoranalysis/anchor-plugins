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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.feature.bean.histogram.Mean;
import org.anchoranalysis.image.feature.input.FeatureInputHistogram;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * Calculates a statistic from the intensity values covered by a single object-mask in a channel.
 *
 * <p>Specifically, a histogram of intensity-values is constructed for the region covered by the
 * object in one specific channnel in the energy-stack (specified by <b>energyIndex</b>).
 *
 * <p>Then a customizable {@link org.anchoranalysis.image.feature.bean.FeatureHistogram} (specified
 * by <b>item</b>) extracts a statistic from the histogram. By default, the <i>mean</i> is
 * calculated.
 *
 * @author Owen Feehan
 */
public class Intensity extends FeatureEnergyChannel {

    // START BEAN PROPERTIES
    /** Feature to apply to the histogram */
    @BeanField @Getter @Setter private Feature<FeatureInputHistogram> item = new Mean();

    /** Iff true, zero-valued voxels are excluded from the histogram */
    @BeanField @Getter @Setter private boolean excludeZero = false;
    // END BEAN PROEPRTIES

    @Override
    protected double calculateForChannel(
            SessionInput<FeatureInputSingleObject> input, Channel channel)
            throws FeatureCalculationException {
        return input.forChild()
                .calculate(
                        item,
                        new CalculateHistogramForChannel(excludeZero, getEnergyIndex(), channel),
                        cacheName());
    }

    private ChildCacheName cacheName() {
        return new ChildCacheName(
                Intensity.class, String.valueOf(excludeZero) + "_" + getEnergyIndex());
    }
}
