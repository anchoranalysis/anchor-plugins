/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.nrg;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;

public class CountEqual extends SpecificNRGChannelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int value = BinaryValues.getDefault().getOnInt();
    // END BEAN PROPERTIES

    @Override
    protected double calcWithChannel(ObjectMask object, Channel chnl) throws FeatureCalcException {
        return chnl.getVoxelBox().any().countEqualMask(value, object);
    }
}
