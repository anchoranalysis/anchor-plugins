/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.channel.provider.intensity;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.scale.ScaleFactor;

/**
 * Scales the channel in the X and Y dimensions.
 *
 * @author Owen Feehan
 */
public class ScaleXY extends ChannelProviderUnary {

    // Start BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter
    private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
    // End BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {
        return scale(channel, scaleCalculator, interpolator.create(), getLogger().messageLogger());
    }

    public static Channel scale(
            Channel channel,
            ScaleCalculator scaleCalculator,
            Interpolator interpolator,
            MessageLogger logger)
            throws CreateException {
        try {
            logger.logFormatted("incoming Image Resolution: %s\n", channel.resolution());

            ScaleFactor scaleFactor = scaleCalculator.calculate(Optional.of(channel.dimensions()));

            logger.logFormatted("Scale Factor: %s\n", scaleFactor.toString());

            Channel channelOut = channel.scaleXY(scaleFactor, interpolator);

            logger.logFormatted("outgoing Image Resolution: %s\n", channelOut.resolution());

            return channelOut;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
