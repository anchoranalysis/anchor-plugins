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

package org.anchoranalysis.plugin.image.bean.channel.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;

/**
 * A {@link ChannelProviderUnary} which has a scalar value field.
 *
 * <p>This abstract class extends {@link ChannelProviderUnary} to provide a framework for creating
 * channels based on an input channel and a scalar value. Subclasses must implement the {@code
 * createFromChannelWithConstant} method.
 */
public abstract class UnaryWithValueBase extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /** The scalar value to be used in channel creation. */
    @BeanField @Getter @Setter private double value;

    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {
        return createFromChannelWithConstant(channel, value);
    }

    /**
     * Creates a new channel from an existing channel and a scalar value.
     *
     * @param channel the input {@link Channel}
     * @param value the scalar value to be used in channel creation
     * @return a new {@link Channel} created from the input channel and scalar value
     * @throws ProvisionFailedException if the channel creation fails
     */
    protected abstract Channel createFromChannelWithConstant(Channel channel, double value)
            throws ProvisionFailedException;
}
