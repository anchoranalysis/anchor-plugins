package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxelOffsets;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

final class ConvertAllToConnectedComponents implements ProcessVoxelOffsets {
	
	private final EncodedVoxelBox matS;
	
	private EncodedIntBuffer currentSlice;
	
	public ConvertAllToConnectedComponents(EncodedVoxelBox matS) {
		super();
		this.matS = matS;
	}
	
	@Override
	public void notifyChangeZ(int z) {
		currentSlice = matS.getPixelsForPlane(z);
	}		
	
	@Override
	public void process(Point3i pnt, int offset3d, int offsetSlice) {
		currentSlice.convertCode(
			offsetSlice,
			offset3d,
			matS,
			pnt
		);
	}
}