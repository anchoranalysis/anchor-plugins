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

package org.anchoranalysis.plugin.image.bean.channel.provider.gradient;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.convert.ChannelConverter;
import org.anchoranalysis.image.core.channel.convert.ConversionPolicy;
import org.anchoranalysis.image.core.channel.convert.ToUnsignedByte;
import org.anchoranalysis.image.core.channel.convert.ToUnsignedShort;

/**
 * Base class to calculate the gradient of the voxels in a {@link Channel}.
 *
 * <p>If the existing channel has appropriate voxel data-type, its voxels are replaced, otherwise
 * new voxels are assigned.
 *
 * @author Owen Feehan
 */
public abstract class GradientBase extends ChannelProviderUnary {

    // START BEAN
    /**
     * Scale factor applied to the gradient values.
     *
     * <p>Default value is 1.0.
     */
    @BeanField @Getter @Setter private double scaleFactor = 1.0;

    /**
     * If true, outputs a short channel, otherwise byte channel.
     *
     * <p>Default value is false.
     */
    @BeanField @Getter @Setter private boolean outputShort = false;
    // END BEAN

    /**
     * Converts the input channel to the output type specified by {@link #outputShort}.
     *
     * @param channelToConvert the {@link Channel} to convert
     * @return the converted {@link Channel}
     */
    protected Channel convertToOutputType(Channel channelToConvert) {
        ChannelConverter<?> converter = outputShort ? new ToUnsignedShort() : new ToUnsignedByte();
        return converter.convert(channelToConvert, ConversionPolicy.CHANGE_EXISTING_CHANNEL);
    }
}
