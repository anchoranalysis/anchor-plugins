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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Takes a 2-dimensional mask and converts into a 3-dimensional mask by repeating along the z-stack
public class ChnlProviderExpandSliceToStack extends ChnlProviderDimSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider slice;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromDim(ImageDimensions dimensions) throws CreateException {

        Channel chnl = slice.create();

        ImageDimensions dimensionsSource = chnl.dimensions();

        if (dimensionsSource.x() != dimensions.x()) {
            throw new CreateException("x dimension is not equal");
        }
        if (dimensionsSource.y() != dimensions.y()) {
            throw new CreateException("y dimension is not equal");
        }

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyUninitialised(dimensions, VoxelDataTypeUnsignedByte.INSTANCE);

        Voxels<ByteBuffer> voxelsSlice = chnl.voxels().asByte();
        Voxels<ByteBuffer> voxelsOut = chnlOut.voxels().asByte();

        for (int z = 0; z < chnlOut.dimensions().z(); z++) {
            VoxelBuffer<ByteBuffer> bb = voxelsSlice.duplicate().slice(0);
            voxelsOut.updateSlice(z, bb);
        }

        return chnlOut;
    }
}
