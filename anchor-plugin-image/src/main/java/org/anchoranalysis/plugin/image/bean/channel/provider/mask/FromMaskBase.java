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

package org.anchoranalysis.plugin.image.bean.channel.provider.mask;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;

/**
 * A base class for a {@link ChannelProvider} which also uses a binary-mask, but which doesn't use
 * any other {@link ChannelProvider} as an input.
 *
 * <p>Note for classes that use both a binary-mask AND another {@link ChannelProvider}, see {@link
 * UnaryWithMaskBase}.
 *
 * @author Owen Feehan
 */
public abstract class FromMaskBase extends ChannelProvider {

    // START BEAN PROPERTIES
    /** The {@link MaskProvider} used to create the binary mask. */
    @BeanField @Getter @Setter private MaskProvider mask;

    // END BEAN PROPERTIES

    @Override
    public Channel get() throws ProvisionFailedException {
        Mask maskChannel = mask.get();
        return createFromMask(maskChannel);
    }

    /**
     * Creates a {@link Channel} from the provided {@link Mask}.
     *
     * @param mask the {@link Mask} to create the channel from
     * @return the created {@link Channel}
     * @throws ProvisionFailedException if the channel creation fails
     */
    protected abstract Channel createFromMask(Mask mask) throws ProvisionFailedException;
}
