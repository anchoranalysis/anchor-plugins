package ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.grayscalereconstruction;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePoint;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePointObjectMask;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.image.voxel.nghb.iterator.PointExtntIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointObjMaskIterator;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.PriorityQueueIndexRangeDownhill;

public class GrayscaleReconstructionRobinson extends GrayscaleReconstructionByErosion {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	
	// END BEAN PROPERTIES

	// we now have a markerForReconstruction in the same condition as the 'strong' condition in the Robison paper
	// all pixels are either 0 or their final value (from chnl)
	private VoxelBoxWrapper reconstructionByDilation( VoxelBoxWrapper maskVb, VoxelBoxWrapper markerVb, ObjMask containingMask ) {
		
		// We use this to track if something has been finalized or not
		VoxelBox<ByteBuffer> vbFinalized = VoxelBoxFactory.instance().getByte().create( markerVb.any().extnt() );
		
		
		// TODO make more efficient
		// Find maximum value of makerVb.... we can probably get this elsewhere without having to iterate the image again
		int maxValue = markerVb.any().ceilOfMaxPixel();
		
		PriorityQueueIndexRangeDownhill<Point3i> queue = new PriorityQueueIndexRangeDownhill<>(maxValue);
		
		// TODO make more efficient
		// We put all non-zero pixels in our queue (these correspond to our seeds from our marker, but let's iterate the image again
		//  for sake of keeping modularity
		if (containingMask!=null) {
			populateQueueFromNonZeroPixelsMask( queue, markerVb.any(), vbFinalized, containingMask );
		} else {
			populateQueueFromNonZeroPixels( queue, markerVb.any(), vbFinalized );
		}
		
		readFromQueueUntilEmpty( queue, markerVb.any(), maskVb.any(), vbFinalized, containingMask );
		
		return markerVb;
	}
	
	private void readFromQueueUntilEmpty( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> markerVb, VoxelBox<?> maskVb, VoxelBox<ByteBuffer> vbFinalized, ObjMask containingMask ) {
		
		Extent e = markerVb.extnt();
		
		SlidingBuffer<?> sbMarker = new SlidingBuffer<>(markerVb);
		SlidingBuffer<?> sbMask = new SlidingBuffer<>(maskVb);
		SlidingBuffer<ByteBuffer> sbFinalized = new SlidingBuffer<>(vbFinalized);
		
		BinaryValuesByte bvFinalized = BinaryValuesByte.getDefault();
		
		PointTester pt = new PointTester(sbMarker, sbMask, sbFinalized, queue, bvFinalized );
		PointIterator pointIterator = containingMask!=null ?  new PointObjMaskIterator(pt, containingMask) : new PointExtntIterator( markerVb.extnt(), pt);
		
		Nghb nghb = new BigNghb(false);
		boolean do3D = e.getZ() > 1;
		
		for( int nextVal=queue.nextValue(); nextVal!=-1; nextVal=queue.nextValue() ) {
			
			Point3i pnt = queue.get();
			
			sbMarker.init(pnt.getZ());
			sbMask.init(pnt.getZ());
			sbFinalized.init(pnt.getZ());
			
			// We have a point, and a value
			// Now we iterate through the neighbours (but only if they haven't been finalised)
			pointIterator.initPnt(pnt.getX(), pnt.getY(), pnt.getZ());
			
			int indx = e.offset(pnt.getX(), pnt.getY());
			pt.reset( indx, nextVal );
			
			// Makes sure that it includes its centre point
			
			nghb.processAllPointsInNghb(do3D, pointIterator);
		}
	}
	
	
	private static class PointTester implements IProcessAbsolutePoint, IProcessAbsolutePointObjectMask {

		private SlidingBuffer<?> sbMarker;
		private SlidingBuffer<?> sbMask;
		private SlidingBuffer<ByteBuffer> sbFinalized;
		private PriorityQueueIndexRangeDownhill<Point3i> queue;

		// Current ByteBuffer
		private VoxelBuffer<?> bbMarker;
		private VoxelBuffer<?> bbMask;
		private VoxelBuffer<ByteBuffer> bbFinalized;
		private int z = 0;
		
		private int indx;
		private int exstVal;
		private Extent extnt;
		private final BinaryValuesByte bv;
		
		public PointTester(SlidingBuffer<?> sbMarker, SlidingBuffer<?> sbMask, SlidingBuffer<ByteBuffer> sbFinalized, PriorityQueueIndexRangeDownhill<Point3i> queue, BinaryValuesByte bv) {
			super();
			this.sbMarker = sbMarker;
			this.sbFinalized = sbFinalized;
			this.sbMask = sbMask;
			this.extnt = sbMask.extnt();
			this.queue = queue;
			this.bv = bv;
		}

		
		public void reset( int indx, int exstVal ) {
			this.indx = indx;
			this.exstVal = exstVal;
		}
		
		@Override
		public void notifyChangeZ(int zChange, int z) {
			this.bbMarker = sbMarker.bufferRel(zChange);
			this.bbMask = sbMask.bufferRel(zChange);
			this.bbFinalized = sbFinalized.bufferRel(zChange);
			this.z = z;
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1) {

			// We can replace with local index changes
			int indxChange = extnt.offset(xChange, yChange);
			
			// We see if it's been finalized or not
			
			int indx1 = indx+indxChange;
			
			byte b = bbFinalized.buffer().get(indx1);
			if (b==bv.getOffByte()) {

				// get value from mask
				int maskVal = bbMask.getInt(indx1);
				
				int valToWrite = Math.min(maskVal, exstVal);
				
				// write this value to the output image
				bbMarker.putInt(indx1, valToWrite);
				
				// put the neighbour on the queue
				queue.put(new Point3i(x1,y1,z), valToWrite);
				
				// point as finalized
				bbFinalized.buffer().put( indx1, bv.getOnByte() );
				
				return true;
			}
			
			return false;
		}


		@Override
		public void notifyChangeZ(int zChange, int z,
				ByteBuffer objectMaskBuffer) {
			notifyChangeZ(zChange, z);
		}


		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1,
				int objectMaskOffset) {
			return processPoint(xChange, yChange, x1, y1);
		}
	}
	
	private void populateQueueFromNonZeroPixels( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> vb, VoxelBox<ByteBuffer> vbFinalized ) {
		
		byte maskOn = BinaryValuesByte.getDefault().getOnByte();
		
		Extent e = vb.extnt();
		for (int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbFinalized = vbFinalized.getPixelsForPlane(z);
			
			int offset = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					int val = bb.getInt(offset);
					if (val!=0) {
						
						queue.put( new Point3i(x,y,z), val);	
						bbFinalized.buffer().put(offset, maskOn);
					}
					
					offset++;
				}
			}
		}	
	}
	
	
	private void populateQueueFromNonZeroPixelsMask( PriorityQueueIndexRangeDownhill<Point3i> queue, VoxelBox<?> vb, VoxelBox<ByteBuffer> vbFinalized, ObjMask containingMask ) {

		Point3i crnrPntMin = containingMask.getBoundingBox().getCrnrMin();
		Point3i crnrPntMax = containingMask.getBoundingBox().calcCrnrMax();
		
		byte maskOn = containingMask.getBinaryValuesByte().getOnByte(); 
				
		Extent e = vb.extnt();
		for (int z=crnrPntMin.getZ(); z<=crnrPntMax.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbFinalized = vbFinalized.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbMask = containingMask.getVoxelBox().getPixelsForPlane(z-crnrPntMin.getZ());
			
			int offset = 0;
			for (int y=crnrPntMin.getY(); y<=crnrPntMax.getY(); y++) {
				for (int x=crnrPntMin.getX(); x<=crnrPntMax.getX(); x++) {
					
					if (bbMask.buffer().get(offset)==maskOn) {
						int offsetGlobal = e.offset(x, y);
						int val = bb.getInt(offsetGlobal);
						if (val!=0) {
							queue.put( new Point3i(x,y,z), val);	
							bbFinalized.buffer().put(offsetGlobal, maskOn);
						}
					}
					
					offset++;
				}
			}
		}	
	}
	
	@Override
	public VoxelBoxWrapper reconstruction( VoxelBoxWrapper maskVb, VoxelBoxWrapper markerVb ) {
		
		// We flip everything because the IJ plugin is reconstruction by Dilation, whereas we want reconstruction by Erosion
		markerVb.subtractFromMaxValue();
		maskVb.subtractFromMaxValue();
		
		VoxelBoxWrapper ret = reconstructionByDilation(maskVb, markerVb, null);
		ret.subtractFromMaxValue();
		
		return ret;
	}
	
	@Override
	public VoxelBoxWrapper reconstruction(
			VoxelBoxWrapper maskVb, VoxelBoxWrapper markerVb,
			ObjMask containingMask)
			throws OperationFailedException {

		// We flip everything because original was reconstruction by Dilation, whereas we want reconstruction by Erosion
		markerVb.subtractFromMaxValue();
		maskVb.subtractFromMaxValue();
		
		VoxelBoxWrapper ret = reconstructionByDilation(maskVb, markerVb, containingMask);
		ret.subtractFromMaxValue();
		
		return ret;
	}
}
