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

package org.anchoranalysis.plugin.image.bean.mask.provider.segment;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.plugin.image.channel.DimensionsChecker;

/**
 * Like {@link OptionalProviderFactory} but with specific methods for {@link Channel} and {@link
 * Mask} including checking channel sizes.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ChannelCreatorHelper {

    public static Optional<Channel> createOptionalCheckSize(
            ChannelProvider provider, String providerName, Dimensions dim)
            throws ProvisionFailedException {
        Optional<Channel> channel = OptionalProviderFactory.create(provider);
        if (channel.isPresent()) {
            DimensionsChecker.check(channel.get(), providerName, dim);
        }
        return channel;
    }

    public static Optional<Mask> createOptionalCheckSize(
            MaskProvider provider, String providerName, Dimensions dim)
            throws ProvisionFailedException {
        Optional<Mask> mask = OptionalProviderFactory.create(provider);
        if (mask.isPresent()) {
            DimensionsChecker.check(mask.get(), providerName, dim);
        }
        return mask;
    }
}
