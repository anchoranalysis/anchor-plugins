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

package org.anchoranalysis.plugin.image.bean.mask.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;

/**
 * Base-class for {@link MaskProvider}s that accept a {@link Channel} as input.
 *
 * @author Owen Feehan
 */
public abstract class FromChannelBase extends MaskProvider {

    // START BEAN PROPERTIES
    /** The provider for the channel from which to create the mask. */
    @BeanField @Getter @Setter private ChannelProvider channel;

    // END BEAN PROPERTIES

    @Override
    public Mask get() throws ProvisionFailedException {
        try {
            return createFromSource(channel.get());
        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates a {@link Mask} from the source {@link Channel}.
     *
     * @param source the source channel
     * @return the created mask
     * @throws CreateException if the mask creation fails
     */
    protected abstract Mask createFromSource(Channel source) throws CreateException;
}
