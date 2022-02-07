/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.feature.bean.list;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.bean.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.feature.bean.histogram.Mean;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.plugin.image.feature.bean.histogram.statistic.Kurtosis;
import org.anchoranalysis.plugin.image.feature.bean.histogram.statistic.Skewness;
import org.anchoranalysis.plugin.image.feature.bean.histogram.statistic.StandardDeviation;
import org.anchoranalysis.plugin.image.feature.bean.histogram.statistic.Sum;
import org.anchoranalysis.plugin.image.feature.bean.histogram.statistic.Variance;
import org.anchoranalysis.plugin.image.feature.bean.stack.intensity.Intensity;
import org.apache.commons.lang.StringUtils;

/**
 * Calculate various moment-related statistics about the intensity of each channel of an image.
 *
 * <p>As the features are unaware of the number of channels in a particular stack, a large number of
 * features are created, referring to successive channels. If a feature references a channel that
 * does not exist in a particular image, then {@link Double#NaN} will be calculated.
 *
 * <p>For each channel, the following is calculated:
 *
 * <ol>
 *   <li>sum (zeroth raw moment)
 *   <li>mean (first raw moment)
 *   <li>standard-deviation (square root of the variance)
 *   <li>variance (second central moment)
 *   <li>skewness (normalized third central moment)
 *   <li>kurtosis (standardized fourth central moment)
 * </ol>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Moment_(mathematics)">Moment (mathemathics)</a>
 * @see <a href="https://en.wikipedia.org/wiki/Image_moment">Image moment</a>
 * @author Owen Feehan
 */
public class IntensityMoments extends FeatureListProvider<FeatureInputStack> {

	/** Number of features that are added <i>per channel</i>. */
	private static final int NUMBER_FEATURES = 6;
	
    // START BEAN PROPERTIES
    /**
     * An upper limit on the number of channels in the energy-stack for which we create features.
     */
    @BeanField @Getter @Setter private int maximumNumberChannels = 3;
    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputStack> get() throws ProvisionFailedException {
        FeatureList<FeatureInputStack> out = new FeatureList<>(NUMBER_FEATURES * maximumNumberChannels);

        for (int channelIndex = 0; channelIndex < maximumNumberChannels; channelIndex++) {
            out.addAll(featuresForChannel(channelIndex));
        }

        return out;
    }

    /** Create a list of features for a particular channel in the stack. */
    private FeatureList<FeatureInputStack> featuresForChannel(int channelIndex) {
        FeatureList<FeatureInputStack> out = new FeatureList<>(NUMBER_FEATURES);
        out.add(createFeature(channelIndex, new Sum()));
        out.add(createFeature(channelIndex, new Mean()));
        out.add(createFeature(channelIndex, new StandardDeviation()));
        out.add(createFeature(channelIndex, new Variance()));
        out.add(createFeature(channelIndex, new Skewness()));
        out.add(createFeature(channelIndex, new Kurtosis()));
        return out;
    }

    /**
     * Creates a feature for a particular channel, and using a particular {@code statistic} on the
     * intensity values.
     */
    private FeatureStack createFeature(int channelIndex, FeatureHistogramStatistic statistic) {
        String statisticUncapitalized =
                StringUtils.uncapitalize(statistic.getClass().getSimpleName());

        Intensity intensity = new Intensity();
        intensity.setEnergyIndex(channelIndex);
        intensity.setItem(statistic);
        intensity.setCustomName(
                String.format("channel%d.%s", channelIndex, statisticUncapitalized));
        return intensity;
    }
}
