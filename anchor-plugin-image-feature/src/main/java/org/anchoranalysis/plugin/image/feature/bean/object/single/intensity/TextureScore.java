/* (C)2020 */
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
