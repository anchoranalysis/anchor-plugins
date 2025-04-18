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

package org.anchoranalysis.plugin.image.bean.channel.provider.arithmetic;

import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderTernary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;

/**
 * Creates a new channel that is the mean of three input channels.
 *
 * <p>This class extends {@link ChannelProviderTernary} to compute the pixel-wise mean
 * of three input channels, producing a new channel with unsigned byte voxel type.</p>
 *
 * @author Owen Feehan
 */
public class MeanThreeChannels extends ChannelProviderTernary {

    @Override
    protected Channel process(Channel channel1, Channel channel2, Channel channel3)
            throws ProvisionFailedException {

        checkDimensions(channel1, channel2, channel3);

        Channel channelOut =
                ChannelFactory.instance()
                        .create(channel1.dimensions(), UnsignedByteVoxelType.INSTANCE);

        processVoxels(
                channelOut.voxels().asByte(),
                channel1.voxels().asByte(),
                channel2.voxels().asByte(),
                channel3.voxels().asByte());

        return channelOut;
    }

    /**
     * Processes voxels from three input channels to produce the mean in the output channel.
     *
     * @param voxelsOut the output {@link Voxels} to store the mean values
     * @param voxelsIn1 the first input {@link Voxels}
     * @param voxelsIn2 the second input {@link Voxels}
     * @param voxelsIn3 the third input {@link Voxels}
     */
    private void processVoxels(
            Voxels<UnsignedByteBuffer> voxelsOut,
            Voxels<UnsignedByteBuffer> voxelsIn1,
            Voxels<UnsignedByteBuffer> voxelsIn2,
            Voxels<UnsignedByteBuffer> voxelsIn3) {

        for (int z = 0; z < voxelsOut.extent().z(); z++) {

            UnsignedByteBuffer in1 = voxelsIn1.sliceBuffer(z);
            UnsignedByteBuffer in2 = voxelsIn2.sliceBuffer(z);
            UnsignedByteBuffer in3 = voxelsIn3.sliceBuffer(z);
            UnsignedByteBuffer out = voxelsOut.sliceBuffer(z);

            while (in1.hasRemaining()) {

                int mean = (in1.getUnsigned() + in2.getUnsigned() + in3.getUnsigned()) / 3;

                out.putUnsigned(mean);
            }

            assert (!in2.hasRemaining());
            assert (!in3.hasRemaining());
            assert (!out.hasRemaining());
        }
    }

    /**
     * Checks if the dimensions of all three input channels match.
     *
     * @param channel1 the first {@link Channel}
     * @param channel2 the second {@link Channel}
     * @param channel3 the third {@link Channel}
     * @throws ProvisionFailedException if the dimensions of the channels do not match
     */
    private void checkDimensions(Channel channel1, Channel channel2, Channel channel3)
            throws ProvisionFailedException {

        if (!channel1.dimensions().equals(channel2.dimensions())) {
            throw new ProvisionFailedException("Dimensions of channels do not match");
        }

        if (!channel2.dimensions().equals(channel3.dimensions())) {
            throw new ProvisionFailedException("Dimensions of channels do not match");
        }
    }
}