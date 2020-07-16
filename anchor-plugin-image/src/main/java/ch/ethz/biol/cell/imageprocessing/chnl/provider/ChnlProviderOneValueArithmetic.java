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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.channel.factory.ChannelFactorySingleType;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

/**
 * Base-class for operations that perform a binary arithmetic operation with each voxel-value and a
 * constant.
 *
 * @author Owen Feehan
 */
public abstract class ChnlProviderOneValueArithmetic extends ChnlProviderOneValue {

    private static final ChannelFactorySingleType FACTORY = new ChannelFactoryByte();

    @Override
    public Channel createFromChnlValue(Channel chnl, double value) throws CreateException {

        int constant = (int) value;

        Channel chnlOut = FACTORY.createEmptyInitialised(chnl.getDimensions());

        VoxelBox<?> vbIn = chnl.getVoxelBox().any();
        VoxelBox<?> vbOut = chnlOut.getVoxelBox().any();

        int volumeXY = chnl.getDimensions().getVolumeXY();

        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {

            VoxelBuffer<?> in = vbIn.getPixelsForPlane(z);
            VoxelBuffer<?> out = vbOut.getPixelsForPlane(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int voxelVal = in.getInt(offset);

                int result = binaryOp(voxelVal, constant);

                out.putInt(offset, cropValToByteRange(result));
            }
        }

        return chnlOut;
    }

    /** The binary arithmetic operation that combines the voxel-value and the constant-value */
    protected abstract int binaryOp(int voxel, int constant);

    private static int cropValToByteRange(int result) {

        if (result < 0) {
            return 0;
        }

        if (result > 255) {
            return 255;
        }

        return result;
    }
}
