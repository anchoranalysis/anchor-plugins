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
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

/**
 * From Page 727 from Lin et al (A Multi-Model Approach to Simultaneous Segmentation and
 * Classification of Heterogeneous Populations of Cell Nuclei
 *
 * @author Owen Feehan
 */
public class TextureScore extends FeatureEnergyChannel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int energyIndexGradient = 1;
    // END BEAN PROPERTIES

    @Override
    protected double calculateForChannel(
            FeatureCalculationInput<FeatureInputSingleObject> input, Channel channel)
            throws FeatureCalculationException {

        ObjectMask object = input.get().getObject();
        Channel channelGradient =
                input.get().getEnergyStackRequired().getChannel(energyIndexGradient);

        return scoreFromMeans(
                IntensityMeanCalculator.calculateMeanIntensityObject(channel, object),
                IntensityMeanCalculator.calculateMeanIntensityObject(channelGradient, object));
    }

    private static double scoreFromMeans(double meanIntensity, double meanGradientIntensity) {
        double scaleFactor = 128 / meanIntensity;

        return (scaleFactor * meanGradientIntensity) / meanIntensity;
    }
}
