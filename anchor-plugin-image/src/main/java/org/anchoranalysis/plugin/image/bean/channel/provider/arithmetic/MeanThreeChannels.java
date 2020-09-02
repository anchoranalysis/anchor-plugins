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

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProviderTernary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.PrimitiveConverter;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;

/**
 * Creates a new channel that is the mean of three input channels
 *
 * @author Owen Feehan
 */
public class MeanThreeChannels extends ChannelProviderTernary {

    @Override
    protected Channel process(Channel channel1, Channel channel2, Channel channel3)
            throws CreateException {

        checkDims(channel1, channel2, channel3);

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

    private void processVoxels(
            Voxels<ByteBuffer> voxelsOut,
            Voxels<ByteBuffer> voxelsIn1,
            Voxels<ByteBuffer> voxelsIn2,
            Voxels<ByteBuffer> voxelsIn3) {

        for (int z = 0; z < voxelsOut.extent().z(); z++) {

            ByteBuffer in1 = voxelsIn1.sliceBuffer(z);
            ByteBuffer in2 = voxelsIn2.sliceBuffer(z);
            ByteBuffer in3 = voxelsIn3.sliceBuffer(z);
            ByteBuffer out = voxelsOut.sliceBuffer(z);

            while (in1.hasRemaining()) {

                byte b1 = in1.get();
                byte b2 = in2.get();
                byte b3 = in3.get();

                int i1 = PrimitiveConverter.unsignedByteToInt(b1);
                int i2 = PrimitiveConverter.unsignedByteToInt(b2);
                int i3 = PrimitiveConverter.unsignedByteToInt(b3);

                int mean = (i1 + i2 + i3) / 3;

                out.put((byte) mean);
            }

            assert (!in2.hasRemaining());
            assert (!in3.hasRemaining());
            assert (!out.hasRemaining());
        }
    }

    private void checkDims(Channel channel1, Channel channel2, Channel channel3)
            throws CreateException {

        if (!channel1.dimensions().equals(channel2.dimensions())) {
            throw new CreateException("Dimensions of channels do not match");
        }

        if (!channel2.dimensions().equals(channel3.dimensions())) {
            throw new CreateException("Dimensions of channels do not match");
        }
    }
}
