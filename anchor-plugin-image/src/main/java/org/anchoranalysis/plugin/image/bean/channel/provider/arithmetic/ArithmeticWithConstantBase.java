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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.channel.factory.ChannelFactorySingleType;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsVoxelBoxAsInt;
import org.anchoranalysis.plugin.image.bean.channel.provider.UnaryWithValueBase;

/**
 * Base-class for operations that perform a binary arithmetic operation with each voxel-value and a
 * constant.
 *
 * @author Owen Feehan
 */
public abstract class ArithmeticWithConstantBase extends UnaryWithValueBase {

    private static final ChannelFactorySingleType FACTORY = new ChannelFactoryByte();

    @Override
    public Channel createFromChannelWithConstant(Channel channel, double value)
            throws CreateException {

        int constant = (int) value;

        Channel channelOut = FACTORY.createEmptyInitialised(channel.dimensions());

        IterateVoxelsVoxelBoxAsInt.callEachPointTwo(
                channel.voxels().any(),
                channelOut.voxels().any(),
                (buffer1, buffer2, offset) -> {
                    int voxelVal = buffer1.getInt(offset);

                    int result = performBinaryOperation(voxelVal, constant);

                    buffer2.putInt(offset, cropValueToByteRange(result));
                });

        return channelOut;
    }

    /** The binary arithmetic operation that combines the voxel-value and the constant-value */
    protected abstract int performBinaryOperation(int voxel, int constant);

    private static int cropValueToByteRange(int result) {

        if (result < 0) {
            return 0;
        }

        if (result > 255) {
            return 255;
        }

        return result;
    }
}
