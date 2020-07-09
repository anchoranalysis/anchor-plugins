package org.anchoranalysis.plugin.mpp.bean.proposer.points;

/*-
 * #%L
 * anchor-plugin-mpp
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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class PointsFromInsideHelper {

	// TODO horribly long, refactor
	public static List<Point3i> convexOnly(
		BinaryChnl chnl,
		BinaryChnl chnlFilled,
		BoundingBox bbox,
		Point3d pntRoot,
		PointListForConvex pntsConvexRoot,
		int skipAfterSuccessiveEmptySlices
	) {
		Preconditions.checkArgument( chnl.getDimensions().contains(bbox) );
	
		int startZ = (int) Math.floor(pntRoot.getZ());
	
		BinaryVoxelBox<ByteBuffer> binaryVoxelBox = chnlFilled.binaryVoxelBox();
		
		List<Point3i> listOut = new ArrayList<>();
		
		BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
		VoxelBox<ByteBuffer> vb = chnl.getChannel().getVoxelBox().asByte();
		
		// Stays as -1 until we reach a non-empty slice
		int successiveEmptySlices = -1;
			
		Extent e = vb.extent();
		ReadableTuple3i crnrMin = bbox.cornerMin();
		ReadableTuple3i crnrMax = bbox.calcCornerMax();
		
		for( int z=startZ; z<=crnrMax.getZ(); z++) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			boolean addedToSlice = false;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int offset = e.offset(x, y);
					if (bb.get(offset)==bvb.getOnByte()) {
						
						Point3i pnt = new Point3i(x,y,z); 
						if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
							addedToSlice = true;
							listOut.add( pnt );
						}
					}
					
				}
			}
			
			if (!addedToSlice) {
				successiveEmptySlices = 0;
				
			// We don't increase the counter until we've been inside a non-empty slice
			} else if (successiveEmptySlices!=-1){	
				successiveEmptySlices++;
				if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
					break;
				}
				
			}
		}
		
		// Exit early if we start on the first slice
		if (startZ==0) {
			return listOut;
		}
		
		for( int z=(startZ-1); z>=crnrMin.getZ(); z--) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			boolean addedToSlice = false;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int offset = e.offset(x, y);
					if (bb.get(offset)==bvb.getOnByte()) {
						
						Point3i pnt = new Point3i(x,y,z);
						
						if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
							addedToSlice = true;
							listOut.add( pnt );
						}
					}
					
				}
			}
			
			if (!addedToSlice) {
				successiveEmptySlices = 0;
				
			// We don't increase the counter until we've been inside a non-empty slice
			} else if (successiveEmptySlices!=-1){	
				successiveEmptySlices++;
				if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
					break;
				}
				
			}
		}
		return listOut;
	}
}
