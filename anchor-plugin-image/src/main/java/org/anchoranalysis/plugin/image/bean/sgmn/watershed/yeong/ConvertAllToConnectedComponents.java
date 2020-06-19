package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.nio.IntBuffer;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxelSliceBuffer;
import org.anchoranalysis.plugin.image.sgmn.watershed.encoding.EncodedIntBuffer;
import org.anchoranalysis.plugin.image.sgmn.watershed.encoding.EncodedVoxelBox;

final class ConvertAllToConnectedComponents implements ProcessVoxelSliceBuffer<IntBuffer> {
	
	private final EncodedVoxelBox matS;
	private final Extent extent;
	
	/** A 3D offset for the 0th pixel in the current slice. */
	private int offsetZ = 0;
	
	public ConvertAllToConnectedComponents(EncodedVoxelBox matS) {
		super();
		this.matS = matS;
		this.extent = matS.extent();
	}
	
	@Override
	public void notifyChangeZ(int z) {
		offsetZ = extent.offset(0, 0, z);
	}		
	
	@Override
	public void process(Point3i pnt, IntBuffer buffer, int offsetSlice) {
		assert(buffer!=null);
		new EncodedIntBuffer(buffer, matS.getEncoding()).convertCode(
			offsetSlice,
			offsetZ + offsetSlice,
			matS,
			pnt
		);
	}
}