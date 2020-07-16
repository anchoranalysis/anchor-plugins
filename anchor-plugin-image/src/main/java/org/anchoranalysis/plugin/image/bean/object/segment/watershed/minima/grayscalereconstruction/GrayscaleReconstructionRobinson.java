package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.grayscalereconstruction;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;

public class GrayscaleReconstructionRobinson extends GrayscaleReconstructionByErosion {
	
	@Override
	public VoxelBoxWrapper reconstruction( VoxelBoxWrapper maskVb, VoxelBoxWrapper markerVb, Optional<ObjectMask> containingMask ) {
		
		// We flip everything because what occurs is erosion by Dilation, whereas we want reconstruction by Erosion
		markerVb.subtractFromMaxValue();
		maskVb.subtractFromMaxValue();
		
		VoxelBoxWrapper ret = reconstructionByDilation(maskVb, markerVb, containingMask);
		ret.subtractFromMaxValue();
		
		return ret;
	}

	// we now have a markerForReconstruction in the same condition as the 'strong' condition in the Robison paper
	// all pixels are either 0 or their final value (from chnl)
	private VoxelBoxWrapper reconstructionByDilation( VoxelBoxWrapper maskVb, VoxelBoxWrapper markerVb, Optional<ObjectMask> containingMask ) {
		
		// We use this to track if something has been finalized or not
		VoxelBox<ByteBuffer> vbFinalized = VoxelBoxFactory.getByte().create( markerVb.any().extent() );
		
		
		// TODO make more efficient
		// Find maximum value of makerVb.... we can probably get this elsewhere without having to iterate the image again
		int maxValue = markerVb.any().ceilOfMaxPixel();
		
		PriorityQueueIndexRangeDownhill<Point3i> queue = new PriorityQueueIndexRangeDownhill<>(maxValue);
		
		// TODO make more efficient
		// We put all non-zero pixels in our queue (these correspond to our seeds from our marker, but let's iterate the image again
		//  for sake of keeping modularity
		if (containingMask.isPresent()) {
			populateQueueFromNonZeroPixelsMask( queue, markerVb.any(), vbFinalized, containingMask.get() );
		} else {
			populateQueueFromNonZeroPixels( queue, markerVb.any(), vbFinalized );
		}
		
		readFromQueueUntilEmpty( queue, markerVb.any(), maskVb.any(), vbFinalized, containingMask );
		
		return markerVb;
	}
	
	private void readFromQueueUntilEmpty( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> markerVb, VoxelBox<?> maskVb, VoxelBox<ByteBuffer> vbFinalized, Optional<ObjectMask> containingMask ) {
		
		Extent extent = markerVb.extent();
		
		SlidingBuffer<?> sbMarker = new SlidingBuffer<>(markerVb);
		SlidingBuffer<?> sbMask = new SlidingBuffer<>(maskVb);
		SlidingBuffer<ByteBuffer> sbFinalized = new SlidingBuffer<>(vbFinalized);
		
		BinaryValuesByte bvFinalized = BinaryValuesByte.getDefault();

		ProcessVoxelNeighbor<?> process = ProcessVoxelNeighborFactory.within(
			containingMask,
			extent,
			new PointProcessor(sbMarker, sbMask, sbFinalized, queue, bvFinalized )
		);
		
		Neighborhood neighborhood = NeighborhoodFactory.of(false);
		boolean do3D = extent.getZ() > 1;
		
		for( int nextVal=queue.nextValue(); nextVal!=-1; nextVal=queue.nextValue() ) {
			
			Point3i point = queue.get();
			
			sbMarker.seek(point.getZ());
			sbMask.seek(point.getZ());
			sbFinalized.seek(point.getZ());
			
			// We have a point, and a value
			// Now we iterate through the neighbors (but only if they haven't been finalised)
			// Makes sure that it includes its center point
			IterateVoxels.callEachPointInNeighborhood(point, neighborhood, do3D, process, nextVal, extent.offsetSlice(point));
		}
	}
		
	private void populateQueueFromNonZeroPixels( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> vb, VoxelBox<ByteBuffer> vbFinalized ) {
		
		byte maskOn = BinaryValuesByte.getDefault().getOnByte();
		
		Extent e = vb.extent();
		for (int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbFinalized = vbFinalized.getPixelsForPlane(z);
			
			int offset = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {

					{
						int val = bb.getInt(offset);
						if (val!=0) {
							queue.put( new Point3i(x,y,z), val);	
							bbFinalized.buffer().put(offset, maskOn);
						}
					}
					
					offset++;
				}
			}
		}	
	}
	
	
	private void populateQueueFromNonZeroPixelsMask( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> vb, VoxelBox<ByteBuffer> vbFinalized, ObjectMask containingMask ) {

		ReadableTuple3i crnrpointMin = containingMask.getBoundingBox().cornerMin();
		ReadableTuple3i crnrpointMax = containingMask.getBoundingBox().calcCornerMax();
		
		byte maskOn = containingMask.getBinaryValuesByte().getOnByte(); 
				
		Extent e = vb.extent();
		for (int z=crnrpointMin.getZ(); z<=crnrpointMax.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbFinalized = vbFinalized.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbMask = containingMask.getVoxelBox().getPixelsForPlane(z-crnrpointMin.getZ());
			
			int offset = 0;
			for (int y=crnrpointMin.getY(); y<=crnrpointMax.getY(); y++) {
				for (int x=crnrpointMin.getX(); x<=crnrpointMax.getX(); x++) {
					if (bbMask.buffer().get(offset)==maskOn) {
						
						int offsetGlobal = e.offset(x, y);
					
						{
							int val = bb.getInt(offsetGlobal);
							if (val!=0) {
								queue.put( new Point3i(x,y,z), val);	
								bbFinalized.buffer().put(offsetGlobal, maskOn);
							}
						}
					}
					
					offset++;
				}
			}
		}	
	}
}
