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

package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderHolder;
import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderOutline;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class StackProviderOutlineRGB extends StackProviderWithBackground {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider mask;

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider chnlBlue;

    @BeanField @Getter @Setter private boolean mip = false;

    @BeanField @Getter @Setter private boolean force2D = false;

    @BeanField @Getter @Setter private boolean createShort = false;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        Mask maskChannel = mask.create();

        try {
            boolean do3D = !mip || maskChannel.getDimensions().getZ() == 1;

            return CalcOutlineRGB.apply(
                    calcOutline(maskChannel),
                    backgroundStack(do3D),
                    createBlue(do3D, maskChannel.getDimensions()),
                    createShort);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private Channel createBlue(boolean do3D, ImageDimensions dim) throws CreateException {
        Channel blue = createBlueMaybeProvider(dim);
        if (do3D) {
            return blue;
        } else {
            return blue.maxIntensityProjection();
        }
    }

    private Channel createBlueMaybeProvider(ImageDimensions dim) throws CreateException {
        if (chnlBlue != null) {
            return chnlBlue.create();
        } else {
            return ChannelFactory.instance()
                    .createEmptyInitialised(dim, VoxelDataTypeUnsignedByte.INSTANCE);
        }
    }

    private Mask calcOutline(Mask mask) throws OperationFailedException {
        Mask maskIn = mip ? mask.maxIntensityProj() : mask.duplicate();

        // We calculate outline of mask
        return createOutline(maskIn);
    }

    private Mask createOutline(Mask maskIn) throws OperationFailedException {
        // We calculate outline of mask
        BinaryChnlProviderOutline cpOutline = new BinaryChnlProviderOutline();
        cpOutline.setForce2D(force2D);
        cpOutline.setBinaryChnl(new BinaryChnlProviderHolder(maskIn));
        try {
            cpOutline.initRecursive(getInitializationParameters(), getLogger());
            return cpOutline.create();
        } catch (InitException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
