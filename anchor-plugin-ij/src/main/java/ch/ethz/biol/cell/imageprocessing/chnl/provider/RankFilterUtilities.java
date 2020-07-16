/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class RankFilterUtilities {

    /** Applies a 2D rank-filter independently to each z-slice */
    public static Channel applyEachSlice(Channel chnl, int radius, int filterType) {

        RankFilters rankFilters = new RankFilters();

        VoxelBoxWrapper vb = chnl.getVoxelBox();

        // Are we missing a Z slice?
        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {

            ImageProcessor processor = IJWrap.imageProcessor(vb, z);
            rankFilters.rank(processor, radius, filterType);
        }

        return chnl;
    }
}
