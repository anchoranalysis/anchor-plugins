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
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes a 2-dimensional mask and converts into a 3-dimensional mask along the z-stack but discards
 * empty slices in a binary on the top and bottom
 */
public class ChnlProviderExpandSliceToMask extends ChannelProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnlTargetDimensions;

    @BeanField @Getter @Setter private ChannelProvider chnlSlice;
    // END BEAN PROPERTIES

    @Override
    public Channel create() throws CreateException {

        ImageDimensions dimensionsTarget = chnlTargetDimensions.create().dimensions();

        Channel slice = chnlSlice.create();

        checkDimensions(slice.dimensions(), dimensionsTarget);

        try {
            return createExpandedChnl(dimensionsTarget, slice.voxels().asByte());
        } catch (IncorrectVoxelDataTypeException e) {
            throw new CreateException("chnlSlice must have unsigned 8 bit data");
        }
    }

    private static void checkDimensions(ImageDimensions dimSrc, ImageDimensions dimTarget)
            throws CreateException {
        if (dimSrc.x() != dimTarget.x()) {
            throw new CreateException("x dimension is not equal");
        }
        if (dimSrc.y() != dimTarget.y()) {
            throw new CreateException("y dimension is not equal");
        }
    }

    private Channel createExpandedChnl(ImageDimensions dimensionsTarget, Voxels<ByteBuffer> voxelsSlice) {

        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyUninitialised(dimensionsTarget, VoxelDataTypeUnsignedByte.INSTANCE);

        Voxels<ByteBuffer> voxelsOut = chnl.voxels().asByte();

        for (int z = 0; z < chnl.dimensions().z(); z++) {
            ByteBuffer bb = voxelsSlice.duplicate().slice(0).buffer();
            voxelsOut.updateSlice(z, VoxelBufferByte.wrap(bb));
        }

        return chnl;
    }
}
