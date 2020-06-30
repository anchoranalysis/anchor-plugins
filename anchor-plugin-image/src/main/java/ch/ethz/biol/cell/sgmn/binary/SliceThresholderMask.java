package ch.ethz.biol.cell.sgmn.binary;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.nio.ByteBuffer;

import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

final class SliceThresholderMask extends SliceThresholder {

	private boolean clearOutsideMask;
	private ReadableTuple3i crnrMin;
	private ReadableTuple3i crnrMax;
	private ObjectMask objMask;
	
	public SliceThresholderMask(boolean clearOutsideMask, ObjectMask objMask, BinaryValuesByte bvb) {
		super(bvb);
		this.clearOutsideMask = clearOutsideMask;
		this.objMask = objMask;
		this.crnrMin = objMask.getBoundingBox().getCornerMin();
		this.crnrMax = objMask.getBoundingBox().calcCornerMax();
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
				voxelBoxIn.extent(),
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
