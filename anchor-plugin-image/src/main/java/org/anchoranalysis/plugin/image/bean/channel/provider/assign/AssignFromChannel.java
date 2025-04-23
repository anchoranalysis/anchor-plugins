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

package org.anchoranalysis.plugin.image.bean.channel.provider.assign;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.channel.provider.mask.UnaryWithMaskBase;
import org.anchoranalysis.plugin.image.channel.DimensionsChecker;

/**
 * Copies the pixels from {@code channelAssignFrom} to {@code channel} (possibly masking).
 *
 * <p>The input channel is changed (mutable). The {@code channelAssignFrom} is unchanged
 * (immutable).
 *
 * <p>This class extends {@link UnaryWithMaskBase} to perform pixel assignment from one channel to
 * another, optionally restricted by a mask.
 *
 * @author Owen Feehan
 */
public class AssignFromChannel extends UnaryWithMaskBase {

    // START BEAN PROPERTIES
    /** The channel provider from which pixels will be assigned. */
    @BeanField @Getter @Setter private ChannelProvider channelAssignFrom;

    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChannel(Channel channel, Mask mask)
            throws ProvisionFailedException {

        assign(
                channel,
                DimensionsChecker.createSameSize(channelAssignFrom, "channelAssignFrom", channel),
                mask);

        return channel;
    }

    /**
     * Assigns pixels from one channel to another, restricted by a mask.
     *
     * @param assignTo the {@link Channel} to which pixels will be assigned
     * @param assignFrom the {@link Channel} from which pixels will be assigned
     * @param mask the {@link Mask} restricting the assignment area
     */
    private void assign(Channel assignTo, Channel assignFrom, Mask mask) {

        ObjectMask object = new ObjectMask(mask.binaryVoxels());

        assignFrom
                .voxels()
                .asByte()
                .extract()
                .objectCopyTo(object, assignTo.voxels().asByte(), object.boundingBox());
    }
}
