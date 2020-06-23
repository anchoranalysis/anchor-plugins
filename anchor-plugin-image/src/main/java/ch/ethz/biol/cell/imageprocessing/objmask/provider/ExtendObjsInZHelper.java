package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import java.util.Iterator;
import java.util.stream.IntStream;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objectmask.ObjectMask;

class ExtendObjsInZHelper {

	private ExtendObjsInZHelper() {}
	
	public static ObjectMask createExtendedObj( ObjectMask omFlat, ObjectMask container, BoundingBox bbox, int zCent ) throws CreateException {
		
		Extent extent = bbox.extent();
		
		ObjectMask omNew = container.region(bbox,false);
		
		ByteBuffer bbFlat = omFlat.getVoxelBox().getPixelsForPlane(0).buffer();
				
		int zLow = bbox.getCrnrMin().getZ();
		int zHigh = bbox.calcCrnrMax().getZ();
		
		if (zCent>zHigh) { zCent = zHigh; }
		if (zCent<zLow) { zCent = zLow; }
		
		extend( bbFlat, extent, omNew, omFlat, zLow, IntStream.range(zCent, zHigh+1) );
		extend( bbFlat, extent, omNew, omFlat, zLow, revRange(zLow, zCent) );
		return omNew; 
	}
	
	private static boolean extend( ByteBuffer bbFlat, Extent e, ObjectMask omNew, ObjectMask omFlat, int zLow, IntStream zRange ) {
		
		boolean andMode = true;
		boolean writtenOneSlice = false;
		
		int volumeXY = e.getVolumeXY();
		
		// Start in the mid point, and go upwards
		Iterator<Integer> itr = zRange.iterator();
		while(itr.hasNext()) {
			
			int z = itr.next();
			int zRel = z - zLow;
			
			// We want to set to the Flat version ANDed with
			ByteBuffer bbExst = omNew.getVoxelBox().getPixelsForPlane(zRel).buffer();
			
			if (andMode) {
			
				if (bufferLogicalAnd( volumeXY, bbExst, bbFlat, omNew.getBinaryValuesByte(), omFlat.getBinaryValuesByte() )) {
					writtenOneSlice = true;
				} else {
					// As soon as we have no pixel high, we switch to simply clearing instead, so long as we've written a slice before
					if (writtenOneSlice) {
						andMode = false;
					}
				}
				
			} else {
				setBufferLow( e.getVolumeXY(), bbExst, omNew.getBinaryValuesByte() );
			}
		};
		
		return writtenOneSlice;
	}
		
	private static void setBufferLow( int numVoxels, ByteBuffer buffer, BinaryValuesByte bvb ) {
		for( int i=0; i<numVoxels; i++) {
			buffer.put(i, bvb.getOffByte());
		}
	}
	
	/** Does a logical AND between buffer and receive.  The result is placed in buffer.  receive is unchanged
	  * Returns TRUE if at least one pixel is HIGH, or false otherwise
	  */
	private static boolean bufferLogicalAnd( int numVoxels, ByteBuffer buffer, ByteBuffer receive, BinaryValuesByte bvb, BinaryValuesByte bvbReceive ) {
		
		boolean atLeastOneHigh = false;
		
		for( int i=0; i<numVoxels; i++) {
			
			byte byteBuffer = buffer.get(i);
			byte byteReceive = receive.get(i);
			
			if (byteBuffer==bvb.getOnByte() && byteReceive==bvbReceive.getOnByte()) {
				// No need to change buffer, as byte is already HIGH
				atLeastOneHigh = true;
			} else {
				buffer.put(i, bvb.getOffByte());	
			}
		}
		
		return atLeastOneHigh;
	}
	
	/** Like @{link range} in {@link IntStream} but in reverse order */
	private static IntStream revRange(int from, int to) {
	    return IntStream.range(from, to).map(i -> to - i + from - 1);
	}
}
