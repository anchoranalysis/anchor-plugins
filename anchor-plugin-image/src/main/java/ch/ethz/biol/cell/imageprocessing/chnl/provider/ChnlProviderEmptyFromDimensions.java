/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

public class ChnlProviderEmptyFromDimensions extends ChnlProviderDimSource {

    // START BEAN PROPERTIES
    @BeanField private int value;

    @BeanField private boolean createShort; // If True creates an unsigned short-image
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromDim(ImageDimensions dim) {

        VoxelDataType typeOut =
                createShort
                        ? VoxelDataTypeUnsignedShort.INSTANCE
                        : VoxelDataTypeUnsignedByte.INSTANCE;

        Channel chnlNew = ChannelFactory.instance().createEmptyInitialised(dim, typeOut);
        if (value != 0) {
            chnlNew.getVoxelBox().any().setAllPixelsTo(value);
        }
        return chnlNew;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isCreateShort() {
        return createShort;
    }

    public void setCreateShort(boolean createShort) {
        this.createShort = createShort;
    }
}
