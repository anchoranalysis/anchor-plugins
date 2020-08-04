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
import org.anchoranalysis.image.bean.provider.ChnlProviderTwo;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes the two channels and creates a NEW third channel whose pixels are a function of the two
 * channels
 *
 * <p>Both the two input channels and the output channel are identically-sized.
 *
 * @author Owen Feehan
 */
public abstract class ChnlProviderTwoVoxelMapping extends ChnlProviderTwo {

    @Override
    protected Channel process(Channel chnl1, Channel chnl2) throws CreateException {

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                chnl1.getDimensions(), VoxelDataTypeUnsignedByte.INSTANCE);

        processVoxelBox(
                chnlOut.voxels().asByte(),
                chnl1.voxels().asByte(),
                chnl2.voxels().asByte());

        return chnlOut;
    }

    protected abstract void processVoxelBox(
            VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2);
}
