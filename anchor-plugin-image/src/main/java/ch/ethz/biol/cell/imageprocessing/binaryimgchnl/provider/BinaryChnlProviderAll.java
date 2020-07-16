/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderAll extends BinaryChnlProviderDimSource {

    @Override
    protected Mask createFromSource(ImageDimensions dimSource) throws CreateException {

        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyInitialised(dimSource, VoxelDataTypeUnsignedByte.INSTANCE);

        Mask bic = new Mask(chnl, BinaryValues.getDefault());
        bic.binaryVoxelBox().setAllPixelsToOn();
        return bic;
    }
}
