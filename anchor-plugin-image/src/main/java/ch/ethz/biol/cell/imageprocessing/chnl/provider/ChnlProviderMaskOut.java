/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/**
 * Set pixels NOT IN the mask to 0, but keep pixels IN the mask identical.
 *
 * <p>It's an immutable operation and a new channel is always produced.
 *
 * @author Owen Feehan
 */
public class ChnlProviderMaskOut extends ChnlProviderOneMask {

    @Override
    protected Channel createFromMaskedChnl(Channel chnl, Mask mask) throws CreateException {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().getVoxelBox().asByte();

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(chnl.getDimensions(), chnl.getVoxelDataType());

        BoundingBox bbox = new BoundingBox(chnlOut.getDimensions().getExtent());
        chnl.getVoxelBox()
                .copyPixelsToCheckMask(
                        bbox,
                        chnlOut.getVoxelBox(),
                        bbox,
                        vbMask,
                        mask.getBinaryValues().createByte());

        return chnlOut;
    }
}
