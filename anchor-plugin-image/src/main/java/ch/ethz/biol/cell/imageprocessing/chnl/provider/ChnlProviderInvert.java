/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderInvert extends ChnlProviderOne {

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        VoxelBoxWrapper vb = chnl.getVoxelBox();

        int maxVal = (int) vb.getVoxelDataType().maxValue();

        int volumeXY = vb.any().extent().getVolumeXY();

        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {

            VoxelBuffer<?> bb = vb.any().getPixelsForPlane(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int invertedValue = maxVal - bb.getInt(offset);
                bb.putInt(offset, invertedValue);
            }
        }

        return chnl;
    }
}
