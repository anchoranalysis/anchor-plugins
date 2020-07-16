/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

/**
 * Constructs a 'shell' around an object by a number of dilation/erosion operations (not including
 * the original object mask) and measures the mean intensity of this shell
 */
public class IntensityMeanShell extends IntensityMeanShellBase {

    @Override
    protected double calcForShell(ObjectMask object, Channel chnl) throws FeatureCalcException {
        return IntensityMeanCalculator.calcMeanIntensityObject(chnl, object);
    }
}
