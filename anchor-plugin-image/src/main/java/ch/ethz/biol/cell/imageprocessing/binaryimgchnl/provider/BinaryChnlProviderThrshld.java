/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderThrshld extends BinaryChnlProviderChnlSource {

    // START BEAN
    @BeanField private Thresholder thresholder;
    // END BEAN

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {
        BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
        try {
            BinaryVoxelBox<ByteBuffer> bvb =
                    thresholder.threshold(
                            chnlSource.getVoxelBox(), bvOut, Optional.empty(), Optional.empty());
            return new Mask(
                    bvb,
                    chnlSource.getDimensions().getRes(),
                    ChannelFactory.instance().get(VoxelDataTypeUnsignedByte.INSTANCE));
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    public Thresholder getThresholder() {
        return thresholder;
    }

    public void setThresholder(Thresholder thresholder) {
        this.thresholder = thresholder;
    }
}
