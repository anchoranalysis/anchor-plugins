/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Constructs a 'shell' around an object by a number of dilation/erosion operations (not including
 * the original object mask) and measures the mean intensity of this shell
 */
public class IntensityMeanShellMaxSlice extends IntensityMeanShellBase {

    @Override
    protected double calcForShell(ObjectMask object, Channel chnl) throws FeatureCalcException {

        ValueAndIndex vai = StatsHelper.calcMaxSliceMean(chnl, object, false);

        if (vai.getIndex() == -1) {
            return getEmptyValue();
        }

        return vai.getValue();
    }
}
