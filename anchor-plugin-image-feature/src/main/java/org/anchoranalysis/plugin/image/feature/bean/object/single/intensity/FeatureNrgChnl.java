/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * A feature that uses a channel from the NRG-stack as identified by an index
 *
 * @author Owen Feehan
 */
public abstract class FeatureNrgChnl extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** Index of channel to use in the nrg-stack (0 is the first channel, 1 is second etc.) */
    @BeanField @Getter @Setter private int nrgIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        Channel chnl = input.get().getNrgStackRequired().getChnl(nrgIndex);
        return calcForChnl(input, chnl);
    }

    protected abstract double calcForChnl(
            SessionInput<FeatureInputSingleObject> input, Channel chnl) throws FeatureCalcException;
}
