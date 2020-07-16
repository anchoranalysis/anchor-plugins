/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderRepeatSlice extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {

        Channel chnlIn = chnl.getChannel();
        VoxelBox<ByteBuffer> vbIn = chnlIn.getVoxelBox().asByte();

        ImageDimensions dimSource = dim.create();

        if (chnl.getDimensions().getX() != dimSource.getX()
                && chnl.getDimensions().getY() != dimSource.getY()) {
            throw new CreateException("dims do not match");
        }

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(dimSource, VoxelDataTypeUnsignedByte.INSTANCE);
        VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();

        int volumeXY = vbIn.extent().getVolumeXY();
        for (int z = 0; z < chnlOut.getDimensions().getExtent().getZ(); z++) {

            ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();

            ByteBuffer bbIn = vbIn.getPixelsForPlane(0).buffer();
            for (int i = 0; i < volumeXY; i++) {
                bbOut.put(i, bbIn.get(i));
            }
        }

        return new Mask(chnlOut, chnl.getBinaryValues());
    }

    public ImageDimProvider getDim() {
        return dim;
    }

    public void setDim(ImageDimProvider dim) {
        this.dim = dim;
    }
}
