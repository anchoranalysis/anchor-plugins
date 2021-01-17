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

package org.anchoranalysis.plugin.image.bean.channel.provider.slice;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.Extent;

/**
 * Extracts slices from {@code sliceStart} (inclusive) to {@code sliceEnd} (inclusive).
 *
 * <p>If {@code duplicate==true} bean-property will ensure it is duplicated, and each channel has
 * independent copies of the slices. If this is not needed {@code duplicate==false} results in less
 * memory allocation and copying operations.
 *
 * @author Owen Feehan
 */
public class ExtractSliceRange extends ChannelProviderUnary {

    // START BEANS
    /** Slice index to start extracting from (inclusive). */
    @BeanField @Positive @Getter @Setter private int indexStart;

    /** Slice index to end extracting from (inclusive). */
    @BeanField @Positive @Getter @Setter private int indexEnd;

    /** If true, an extracted slice is duplicated before being assigned to the output channel. */
    @BeanField private boolean duplicate = true;
    // END BEANS

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (indexEnd < indexStart) {
            throw new BeanMisconfiguredException(
                    String.format(
                            "indexStart (%d) must be less than indexEnd (%d)",
                            indexStart, indexEnd));
        }
    }

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        ChannelFactoryByte factory = new ChannelFactoryByte();

        Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();

        Extent extent = channel.extent().duplicateChangeZ(indexEnd - indexStart + 1);

        Channel channelOut =
                factory.createEmptyInitialised(new Dimensions(extent, channel.resolution()));

        Voxels<UnsignedByteBuffer> voxelsOut = channelOut.voxels().asByte();
        for (int z = indexStart; z <= indexEnd; z++) {
            voxelsOut.replaceSlice(z - indexStart, voxels.slice(z));
        }
        return channelOut;
    }
}
