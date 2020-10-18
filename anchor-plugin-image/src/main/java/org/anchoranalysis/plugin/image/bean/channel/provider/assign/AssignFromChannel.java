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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.channel.provider.mask.UnaryWithMaskBase;
import org.anchoranalysis.plugin.image.channel.DimensionsChecker;

/**
 * Copies the pixels from {@code channelAssignFrom} to {@code channel} (possibly masking)
 *
 * <p>channel is changed (mutable). channelAssignFrom is unchanged (immutable).
 *
 * @author Owen Feehan
 */
public class AssignFromChannel extends UnaryWithMaskBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider channelAssignFrom;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChannel(Channel channel, Mask mask) throws CreateException {

        assign(
                channel,
                DimensionsChecker.createSameSize(channelAssignFrom, "channelAssignFrom", channel),
                mask);

        return channel;
    }

    private void assign(Channel assignTo, Channel assignFrom, Mask mask) {

        ObjectMask object = new ObjectMask(mask.binaryVoxels());

        assignFrom
                .voxels()
                .asByte()
                .extract()
                .objectCopyTo(object, assignTo.voxels().asByte(), object.boundingBox());
    }
}
