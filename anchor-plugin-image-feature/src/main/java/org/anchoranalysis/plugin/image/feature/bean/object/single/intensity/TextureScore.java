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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

/**
 * From Page 727 from Lin et al (A Multi-Model Approach to Simultaneous Segmentation and
 * Classification of Heterogeneous Populations of Cell Nuclei
 *
 * @author Owen Feehan
 */
public class TextureScore extends FeatureNrgChnl {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int nrgIndexGradient = 1;
    // END BEAN PROPERTIES

    @Override
    protected double calcForChnl(SessionInput<FeatureInputSingleObject> input, Channel chnl)
            throws FeatureCalcException {

        ObjectMask object = input.get().getObject();
        Channel chnlGradient = input.get().getNrgStackRequired().getChnl(nrgIndexGradient);

        return scoreFromMeans(
                IntensityMeanCalculator.calcMeanIntensityObject(chnl, object),
                IntensityMeanCalculator.calcMeanIntensityObject(chnlGradient, object));
    }

    private static double scoreFromMeans(double meanIntensity, double meanGradientIntensity) {
        double scaleFactor = 128 / meanIntensity;

        return (scaleFactor * meanGradientIntensity) / meanIntensity;
    }
}
