/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderEmpty extends BinaryChnlProviderDimSource {

    // START BEAN PROPERTIES
    /** If true binary values are set high when created */
    @BeanField private boolean createOn = false;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(ImageDimensions dimSource) throws CreateException {
        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyInitialised(dimSource, VoxelDataTypeUnsignedByte.INSTANCE);

        BinaryValues bvOut = BinaryValues.getDefault();

        if (createOn) {
            chnl.getVoxelBox().any().setAllPixelsTo(bvOut.getOnInt());
        }
        return new Mask(chnl, bvOut);
    }

    public boolean isCreateOn() {
        return createOn;
    }

    public void setCreateOn(boolean createOn) {
        this.createOn = createOn;
    }
}
