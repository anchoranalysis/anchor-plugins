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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderRepeatSlice extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ImageDimProvider dimensions = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws CreateException {

        Channel chnlIn = mask.channel();
        Voxels<ByteBuffer> voxelsIn = chnlIn.voxels().asByte();

        ImageDimensions dimSource = dimensions.create();

        if (mask.dimensions().x() != dimSource.x()
                && mask.dimensions().y() != dimSource.y()) {
            throw new CreateException("dims do not match");
        }

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(dimSource, VoxelDataTypeUnsignedByte.INSTANCE);
        Voxels<ByteBuffer> voxelsOut = chnlOut.voxels().asByte();

        int volumeXY = voxelsIn.extent().volumeXY();
        for (int z = 0; z < chnlOut.dimensions().extent().z(); z++) {

            ByteBuffer bbOut = voxelsOut.slice(z).buffer();

            ByteBuffer bbIn = voxelsIn.slice(0).buffer();
            for (int i = 0; i < volumeXY; i++) {
                bbOut.put(i, bbIn.get(i));
            }
        }

        return new Mask(chnlOut, mask.binaryValues());
    }
}
