package org.anchoranalysis.plugin.image.bean.channel.provider.convert;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.channel.converter.ConversionPolicy;

public abstract class ConvertBase extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /**
     * If true, the existing channel can be changed. If false, a new channel must be created for any
     * change
     */
    @BeanField @Getter @Setter private boolean changeExisting = false;
    // END BEAN PROPERTIES

    protected ConversionPolicy createPolicy() {
        return changeExisting
                ? ConversionPolicy.CHANGE_EXISTING_CHANNEL
                : ConversionPolicy.DO_NOT_CHANGE_EXISTING;
    }
}
