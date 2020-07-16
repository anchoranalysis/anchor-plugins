/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.process.ImageProcessor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ChnlProviderEdgeFilterIJ extends ChnlProviderOne {

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Channel dup = chnl.duplicate();
        VoxelBoxWrapper vbDup = dup.getVoxelBox();

        for (int z = 0; z < dup.getDimensions().getZ(); z++) {

            ImageProcessor ip = IJWrap.imageProcessor(vbDup, z);
            ip.filter(ImageProcessor.FIND_EDGES);
        }
        return dup;
    }
}
