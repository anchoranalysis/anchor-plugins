package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

final class SliceThresholderMask extends SliceThresholder {

	private boolean clearOutsideMask;
	private Point3i crnrMin;
	private Point3i crnrMax;
	private ObjMask objMask;
	
	public SliceThresholderMask(boolean clearOutsideMask, ObjMask objMask, BinaryValuesByte bvb) {
		super(bvb);
		this.clearOutsideMask = clearOutsideMask;
		this.objMask = objMask;
		this.crnrMin = objMask.getBoundingBox().getCrnrMin();
		this.crnrMax = objMask.getBoundingBox().calcCrnrMax();
	}
	
	@Override
	public void sgmnAll(
		VoxelBox<?> voxelBoxIn,
		VoxelBox<?> vbThrshld,
		VoxelBox<ByteBuffer> voxelBoxOut
	) {
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++ ) {
			
			int relZ = z - crnrMin.getZ();
			
			sgmnSlice(
				voxelBoxIn.extnt(),
				voxelBoxIn.getPixelsForPlane(relZ),
				vbThrshld.getPixelsForPlane(relZ),
				voxelBoxOut.getPixelsForPlane(relZ),
				objMask.getVoxelBox().getPixelsForPlane(z),
				objMask.getBinaryValuesByte()
			);
		}
	}
	
	private void sgmnSlice(
		Extent extent,
		VoxelBuffer<?> vbIn,
		VoxelBuffer<?> vbThrshld,
		VoxelBuffer<ByteBuffer> vbOut,
		VoxelBuffer<ByteBuffer> vbMask,
		BinaryValuesByte bvbMask
	) {
		int offsetMask = 0;
		ByteBuffer out = vbOut.buffer();
		
		for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
			for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
				
				int offset = extent.offset(x, y);
				
				if (vbMask.buffer().get(offsetMask++)==bvbMask.getOffByte()) {
					
					if (clearOutsideMask) {
						writeOffByte(offset, out);
					}
					
					continue;
				}
				
				writeThresholdedByte(offset, out, vbIn, vbThrshld);
			}
		}		
	}
}