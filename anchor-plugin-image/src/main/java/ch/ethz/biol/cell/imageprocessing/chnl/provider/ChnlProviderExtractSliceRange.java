/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderExtractSliceRange extends ChnlProviderOne {

    // START BEANS
    @BeanField @Positive private int sliceStart;

    @BeanField @Positive private int sliceEnd;
    // END BEANS

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        ChannelFactoryByte factory = new ChannelFactoryByte();

        VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

        Extent e = chnl.getDimensions().getExtent().duplicateChangeZ(sliceEnd - sliceStart + 1);

        Channel chnlOut =
                factory.createEmptyInitialised(
                        new ImageDimensions(e, chnl.getDimensions().getRes()));
        VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();

        int volumeXY = vb.extent().getVolumeXY();
        for (int z = sliceStart; z <= sliceEnd; z++) {

            ByteBuffer bbIn = vb.getPixelsForPlane(z).buffer();
            ByteBuffer bbOut = vbOut.getPixelsForPlane(z - sliceStart).buffer();

            for (int i = 0; i < volumeXY; i++) {
                bbOut.put(i, bbIn.get(i));
            }
        }

        return chnlOut;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (sliceEnd < sliceStart) {
            throw new BeanMisconfiguredException("SliceStart must be less than SliceEnd");
        }
    }

    public int getSliceStart() {
        return sliceStart;
    }

    public void setSliceStart(int sliceStart) {
        this.sliceStart = sliceStart;
    }

    public int getSliceEnd() {
        return sliceEnd;
    }

    public void setSliceEnd(int sliceEnd) {
        this.sliceEnd = sliceEnd;
    }
}
