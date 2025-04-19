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

package org.anchoranalysis.plugin.image.bean.stack.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.Provider;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.stack.Stack;

/**
 * Creates a {@link Stack} from a {@link Channel} or {@link Mask} (reusing the voxel buffers).
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class FromChannelOrMask extends StackProvider {

    // START BEAN PROPERTIES
    /** A channel that is provided to the stack. Either this or {@code mask} must be set. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider channel;

    /** A mask that is provided to the stack. Either this or {@code channel} must be set. */
    @BeanField @OptionalBean @Getter @Setter private Provider<Mask> mask;

    /**
     * If true, the output contains three channels (the input and two duplicates) instead of one.
     */
    @BeanField @Getter @Setter private boolean rgb = false;
    // END BEAN PROPERTIES

    /**
     * Creates a new instance with a specified channel provider.
     *
     * @param channel the {@link ChannelProvider} to use.
     */
    public FromChannelOrMask(ChannelProvider channel) {
        this.channel = channel;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (!(channel != null ^ mask != null)) {
            throw new BeanMisconfiguredException("Either channel or mask must be set");
        }
    }

    @Override
    public Stack get() throws ProvisionFailedException {
        return createStackFromChannel(channelFromSource());
    }

    /**
     * Creates either a grayscale or RGB stack from the channel.
     *
     * @param channel the {@link Channel} to create the stack from.
     * @return a new {@link Stack} containing either one or three channels.
     */
    private Stack createStackFromChannel(Channel channel) {
        if (rgb) {
            try {
                return new Stack(true, channel, channel.duplicate(), channel.duplicate());
            } catch (IncorrectImageSizeException | CreateException e) {
                throw new AnchorImpossibleSituationException();
            }
        } else {
            return new Stack(channel);
        }
    }

    /**
     * Identifies a channel from one of two sources (channel or mask).
     *
     * @return the identified {@link Channel}.
     * @throws ProvisionFailedException if the channel cannot be obtained.
     */
    private Channel channelFromSource() throws ProvisionFailedException {
        if (channel != null) {
            return channel.get();
        } else {
            return mask.get().channel();
        }
    }
}
