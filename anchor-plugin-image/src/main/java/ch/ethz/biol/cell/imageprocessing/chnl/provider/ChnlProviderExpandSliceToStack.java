/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Takes a 2-dimensional mask and converts into a 3-dimensional mask by repeating along the z-stack
public class ChnlProviderExpandSliceToStack extends ChnlProviderDimSource {

    // START BEAN PROPERTIES
    @BeanField private ChnlProvider slice;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromDim(ImageDimensions dim) throws CreateException {

        Channel chnl = slice.create();

        ImageDimensions sdSrc = chnl.getDimensions();

        if (sdSrc.getX() != dim.getX()) {
            throw new CreateException("x dimension is not equal");
        }
        if (sdSrc.getY() != dim.getY()) {
            throw new CreateException("y dimension is not equal");
        }

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyUninitialised(dim, VoxelDataTypeUnsignedByte.INSTANCE);

        VoxelBox<ByteBuffer> vbSlice = chnl.getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();

        for (int z = 0; z < chnlOut.getDimensions().getZ(); z++) {
            VoxelBuffer<ByteBuffer> bb = vbSlice.duplicate().getPixelsForPlane(0);
            vbOut.setPixelsForPlane(z, bb);
        }

        return chnlOut;
    }

    public ChnlProvider getSlice() {
        return slice;
    }

    public void setSlice(ChnlProvider slice) {
        this.slice = slice;
    }
}
