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

package org.anchoranalysis.plugin.image.channel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.dimensions.Dimensions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DimensionsChecker {

    public static void check(Channel channel, Mask mask) throws CreateException {
        if (!channel.dimensions().equals(mask.dimensions())) {
            throw new CreateException(
                    String.format(
                            "channel (%s) and mask (%s) must have the same dimensions",
                            channel.dimensions().toString(), mask.dimensions().toString()));
        }
    }

    /**
     * Checks a channel to make sure it's the same size as an an existing channel
     *
     * @param channelToCheck the channel whose size will be compared
     * @param channelToCheckName a user-meaningful string to identify the channelToCheck in error
     *     messages
     * @param dimFromChannel the dimensions it must equal from channel (identified as channel in
     *     error messages)
     * @throws CreateException
     */
    public static void check(
            Channel channelToCheck, String channelToCheckName, Dimensions dimFromChannel)
            throws CreateException {
        check(channelToCheck.dimensions(), channelToCheckName, dimFromChannel);
    }

    /**
     * Checks a channel to make sure it's the same size as an an existing channel
     *
     * @param maskToCheck the channel whose size will be compared
     * @param channelToCheckName a user-meaningful string to identify the channelToCheck in error
     *     messages
     * @param dimensionsFromChannel the dimensions it must equal from channel (identified as channel
     *     in error messages)
     * @throws CreateException
     */
    public static void check(
            Mask maskToCheck, String channelToCheckName, Dimensions dimensionsFromChannel)
            throws CreateException {
        check(maskToCheck.dimensions(), channelToCheckName, dimensionsFromChannel);
    }

    /**
     * Creates a new channel from a provider, making sure it's the same size as an an existing
     * channel
     *
     * @param provider the provider to to create the channel
     * @param providerName a user-meaningful string to identify the provider in error messages
     * @param channelSameSize the channel which it must be the same size as (referred to in error
     *     messages as "channel")
     * @return the newly created channel
     * @throws CreateException
     */
    public static Channel createSameSize(
            ChannelProvider provider, String providerName, Channel channelSameSize)
            throws CreateException {

        Channel channelNew = provider.create();
        check(channelNew, providerName, channelSameSize.dimensions());
        return channelNew;
    }

    /**
     * Creates a new channel from a provider, making sure it's the same size as an an existing
     * channel
     *
     * @param provider the provider to to create the channel
     * @param providerName a user-meaningful string to identify the provider in error messages
     * @param channelSameSize the channel which it must be the same size as (referred to in error
     *     messages as "channel")
     * @return the newly created channel
     * @throws CreateException
     */
    public static Mask createSameSize(
            MaskProvider provider, String providerName, Channel channelSameSize)
            throws CreateException {

        Mask mask = provider.create();
        check(mask, providerName, channelSameSize.dimensions());
        return mask;
    }

    private static void check(
            Dimensions dimensionsToCheck,
            String channelToCheckName,
            Dimensions dimensionsFromChannel)
            throws CreateException {
        if (!dimensionsFromChannel.equals(dimensionsToCheck)) {
            throw new CreateException(
                    String.format(
                            "channel (%s) and %s (%s) must have the same dimensions",
                            dimensionsFromChannel.toString(),
                            channelToCheckName,
                            dimensionsToCheck.toString()));
        }
    }
}
