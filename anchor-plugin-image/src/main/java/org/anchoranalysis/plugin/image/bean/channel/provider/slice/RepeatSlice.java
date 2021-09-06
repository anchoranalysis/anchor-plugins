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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.plugin.image.bean.channel.provider.FromDimensionsBase;

/**
 * Creates a new channel with specific dimensions that repeatedly duplicates a slice from an
 * existing channel
 *
 * <p>The incoming {@code slice} must have the same extent in XY as specified in {@code dimension}.
 *
 * @author Owen Feehan
 */
public class RepeatSlice extends FromDimensionsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider slice;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromDimensions(Dimensions dimensions) throws ProvisionFailedException {

        Channel sliceCreated = slice.get();

        Dimensions dimensionsSource = sliceCreated.dimensions();

        if (dimensionsSource.x() != dimensions.x()) {
            throw new ProvisionFailedException("x dimension is not equal");
        }
        if (dimensionsSource.y() != dimensions.y()) {
            throw new ProvisionFailedException("y dimension is not equal");
        }

        Channel channelOut =
                ChannelFactory.instance()
                        .createUninitialised(dimensions, UnsignedByteVoxelType.INSTANCE);

        Voxels<UnsignedByteBuffer> voxelsSlice = sliceCreated.voxels().asByte();
        Voxels<UnsignedByteBuffer> voxelsOut = channelOut.voxels().asByte();

        voxelsOut
                .extent()
                .iterateOverZ(z -> voxelsOut.replaceSlice(z, voxelsSlice.duplicate().slice(0)));

        return channelOut;
    }
}
