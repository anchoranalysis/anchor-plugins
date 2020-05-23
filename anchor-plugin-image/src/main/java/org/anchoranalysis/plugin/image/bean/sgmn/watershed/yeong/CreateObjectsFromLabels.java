package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.nio.IntBuffer;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxelOffsets;

class CreateObjectsFromLabels implements ProcessVoxelOffsets {

	private final BoundingBoxMap bbm;
	private final VoxelBox<IntBuffer> matS;
	
	private IntBuffer bbS;
	
	public CreateObjectsFromLabels(VoxelBox<IntBuffer> matS, BoundingBoxMap bbm) {
		super();
		this.matS = matS;
		this.bbm = bbm;
	}

	@Override
	public void notifyChangeZ(int z) {
		bbS = matS.getPixelsForPlane(z).buffer();
	}

	@Override
	public void process(Point3i pnt, int offset3d, int offsetSlice) {
		int crntVal = bbS.get(offsetSlice);
		
		int outVal = bbm.addPointForValue(pnt.getX(), pnt.getY(), pnt.getZ(), crntVal) + 1;
		
		bbS.put(offsetSlice, outVal);
		
	}
}