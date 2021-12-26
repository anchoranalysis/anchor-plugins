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
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales the channel in the X and Y dimensions.
 *
 * @author Owen Feehan
 */
public class ScaleXY extends ChannelProviderUnary {

    // Start BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // End BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {
        try {
            return scale(
                    channel,
                    scaleCalculator,
                    interpolator.voxelsResizer(),
                    getInitialization().suggestedResize(),
                    getLogger().messageLogger());
        } catch (InitializeException e) {
            throw new ProvisionFailedException(e);
        }
    }

    public static Channel scale(
            Channel channel,
            ScaleCalculator scaleCalculator,
            VoxelsResizer interpolator,
            Optional<ImageSizeSuggestion> suggestedResize,
            MessageLogger logger)
            throws ProvisionFailedException {
        try {
            logResolution("Incoming", channel, logger);

            ScaleFactor scaleFactor =
                    scaleCalculator.calculate(Optional.of(channel.dimensions()), suggestedResize);

            logger.logFormatted("Scale Factor: %s", scaleFactor.toString());

            Channel channelOut = channel.scaleXY(scaleFactor, interpolator);

            logResolution("Outgoing", channelOut, logger);

            return channelOut;

        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private static void logResolution(String prefix, Channel channel, MessageLogger logger) {
        logger.logFormatted(
                "%s image resolution: %s", prefix, describeResolution(channel.resolution()));
    }

    private static String describeResolution(Optional<Resolution> resolution) {
        return resolution.map(Resolution::toString).orElse("undefined");
    }
}
