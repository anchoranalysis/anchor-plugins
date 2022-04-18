package org.anchoranalysis.plugin.image.task.bean.format.convertstyle;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.plugin.image.task.stack.ChannelGetterForTimepoint;

/**
 * Extracts three channels to make an RGB image.
 *
 * <p>If the expected red/blue/green names in {@link RGBChannelNames} exist, these channels are
 * used. Any other channels are ignored.
 *
 * <p>Otherwise, if exactly three channels exist, without the expected-names then the underlying
 * order is used from the set.
 *
 * <p>If more than three channel exist, the first three are arbitrarily according by the underlying
 * set ordering (usually alphabetical).
 *
 * <p>If a single-channel exists only, it is triplicated to form an RGB.
 *
 * <p>If two channels-exists, a blank is left in the green channel, and the blue and red channel are
 * chosen by the underlying set ordering (usually alphabetical).
 *
 * @author Owen Feehan
 */
public class ExtractToRGB extends ChannelConvertStyle {

    // START BEAN PROPERTIES
    /**
     * If a channel doesn't match an RGB pattern, this conversion-style can be used instead.
     *
     * <p>If unset, an error is instead thrown in this circumstances
     */
    @BeanField @OptionalBean @Getter @Setter
    private ChannelConvertStyle fallback = new IndependentChannels();
    // END BEAN PROPERTIES

    @Override
    public NamedStacks convert(
            Set<String> channelNames, ChannelGetterForTimepoint channelGetter, Logger logger)
            throws OperationFailedException {
        assert (false);
        // TODO implement
        return null;
    }
}
