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

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.CombineTypes;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

public class ChnlProviderMax extends ChnlProviderTwoVoxelMapping {

    public static Channel createMax(Channel chnl1, Channel chnl2) throws CreateException {

        if (!chnl1.dimensions().equals(chnl2.dimensions())) {
            throw new CreateException("Dimensions of channels do not match");
        }

        VoxelDataType combinedType =
                CombineTypes.combineTypes(chnl1.getVoxelDataType(), chnl2.getVoxelDataType());
        Channel chnlOut =
                ChannelFactory.instance()
                        .create(chnl1.dimensions(), combinedType);

        setMaxInOutputVoxels(
                chnlOut.voxels().asByte(),
                chnl1.voxels().asByte(),
                chnl2.voxels().asByte());

        return chnlOut;
    }

    @Override
    protected void processVoxels(
            Voxels<ByteBuffer> voxelsOut, Voxels<ByteBuffer> voxelsIn1, Voxels<ByteBuffer> voxelsIn2) {
        setMaxInOutputVoxels(voxelsOut, voxelsIn1, voxelsIn2);
    }

    private static void setMaxInOutputVoxels(
            Voxels<ByteBuffer> voxelsOut, Voxels<ByteBuffer> voxelsIn1, Voxels<ByteBuffer> voxelsIn2) {
        int volumeXY = voxelsIn1.extent().volumeXY();
        for (int z = 0; z < voxelsOut.extent().z(); z++) {

            VoxelBuffer<?> in1 = voxelsIn1.slice(z);
            VoxelBuffer<?> in2 = voxelsIn2.slice(z);
            VoxelBuffer<?> out = voxelsOut.slice(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int val1 = in1.getInt(offset);
                int val2 = in2.getInt(offset);

                if (val1 > val2) {
                    out.putInt(offset, val1);
                } else {
                    out.putInt(offset, val2);
                }
            }
        }
    }
}
