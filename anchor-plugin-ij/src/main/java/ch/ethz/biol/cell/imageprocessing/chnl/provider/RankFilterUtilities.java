package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

class RankFilterUtilities {

	private RankFilterUtilities() {}
	
	/** Applies a 2D rank-filter independently to each z-slice */
	public static Channel applyEachSlice( Channel chnl, int radius, int filterType ) throws CreateException {
		
		RankFilters rankFilters = new RankFilters();
		
		VoxelBoxWrapper vb = chnl.getVoxelBox();
		
		// Are we missing a Z slice?
		for (int z=0; z<chnl.getDimensions().getZ(); z++) {
		
			ImageProcessor processor = IJWrap.imageProcessor(vb, z);
			rankFilters.rank( processor, radius, filterType);
		}
		
		return chnl;
	}
}
