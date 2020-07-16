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
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes a 2-dimensional mask and converts into a 3-dimensional mask along the z-stack but discards
 * empty slices in a binary on the top and bottom
 */
public class ChnlProviderExpandSliceToMask extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField private ChnlProvider chnlTargetDimensions;

    @BeanField private ChnlProvider chnlSlice;
    // END BEAN PROPERTIES

    @Override
    public Channel create() throws CreateException {

        ImageDimensions sdTarget = chnlTargetDimensions.create().getDimensions();

        Channel slice = chnlSlice.create();

        checkDimensions(slice.getDimensions(), sdTarget);

        try {
            return createExpandedChnl(sdTarget, slice.getVoxelBox().asByte());
        } catch (IncorrectVoxelDataTypeException e) {
            throw new CreateException("chnlSlice must have unsigned 8 bit data");
        }
    }

    private static void checkDimensions(ImageDimensions dimSrc, ImageDimensions dimTarget)
            throws CreateException {
        if (dimSrc.getX() != dimTarget.getX()) {
            throw new CreateException("x dimension is not equal");
        }
        if (dimSrc.getY() != dimTarget.getY()) {
            throw new CreateException("y dimension is not equal");
        }
    }

    private Channel createExpandedChnl(ImageDimensions sdTarget, VoxelBox<ByteBuffer> vbSlice) {

        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyUninitialised(sdTarget, VoxelDataTypeUnsignedByte.INSTANCE);

        VoxelBox<ByteBuffer> vbOut = chnl.getVoxelBox().asByte();

        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {
            ByteBuffer bb = vbSlice.duplicate().getPixelsForPlane(0).buffer();
            vbOut.setPixelsForPlane(z, VoxelBufferByte.wrap(bb));
        }

        return chnl;
    }
}
