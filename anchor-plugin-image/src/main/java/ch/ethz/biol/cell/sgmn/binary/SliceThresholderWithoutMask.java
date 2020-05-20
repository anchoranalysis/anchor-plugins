package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;

import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class SliceThresholderWithoutMask extends SliceThresholder {

	public SliceThresholderWithoutMask(BinaryValuesByte bvb) {
		super(bvb);
	}

	@Override
	public void sgmnAll(
		VoxelBox<?> voxelBoxIn,
		VoxelBox<?> vbThrshld,
		VoxelBox<ByteBuffer> voxelBoxOut		
	) {
		for( int z=0; z<voxelBoxIn.extnt().getZ(); z++ ) {
			sgmnSlice(
				voxelBoxIn.extnt(),
				voxelBoxIn.getPixelsForPlane(z),
				vbThrshld.getPixelsForPlane(z),
				voxelBoxOut.getPixelsForPlane(z)
			);
		}
	}
	
	private void sgmnSlice(
		Extent extent,
		VoxelBuffer<?> vbIn,
		VoxelBuffer<?> vbThrshld,
		VoxelBuffer<ByteBuffer> bbOut
	) {
		ByteBuffer out = bbOut.buffer();
		
		int offset = 0;
		for( int y=0; y<extent.getY(); y++) {
			for( int x=0; x<extent.getX(); x++) {
				writeThresholdedByte(offset, out, vbIn, vbThrshld);
				offset++;
			}
		}
	}
}
