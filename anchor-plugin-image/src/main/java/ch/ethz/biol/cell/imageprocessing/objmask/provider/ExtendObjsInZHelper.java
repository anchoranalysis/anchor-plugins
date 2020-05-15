package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.nio.ByteBuffer;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;

class ExtendObjsInZHelper {

	private ExtendObjsInZHelper() {}
	
	public static ObjMask createExtendedObj( ObjMask omFlat, ObjMask container, BoundingBox bbox, int zCent ) throws CreateException {
		
		Extent extent = bbox.extnt();
		
		ObjMask omNew = container.createSubmaskAlwaysNew(bbox);
		
		ByteBuffer bbFlat = omFlat.getVoxelBox().getPixelsForPlane(0).buffer();
				
		int zLow = bbox.getCrnrMin().getZ();
		int zHigh = bbox.calcCrnrMax().getZ();
		
		if (zCent>zHigh) { zCent = zHigh; }
		if (zCent<zLow) { zCent = zLow; }
		
		//zCent = ((int) omNew.centerOfGravity().z);
		extendHigh( bbFlat, extent, omNew, omFlat, zLow, zCent, zHigh );
		extendLow( bbFlat, extent, omNew, omFlat, zLow, zCent, zHigh );

		return omNew; 
	}
	
	private static boolean extendHigh( ByteBuffer bbFlat, Extent e, ObjMask omNew, ObjMask omFlat, int zLow, int zCent, int zHigh ) {
		
		boolean andMode = true;
		boolean writtenOneSlice = false;
		
		// Start in the mid point, and go upwards
		for( int z=zCent; z<=zHigh;z++ ) {
				
			int zRel = z - zLow;
			
			// We want to set to the Flat version ANDed with
			ByteBuffer bbExst = omNew.getVoxelBox().getPixelsForPlane(zRel).buffer();
			
			if (andMode) {
			
				if (bufferLogicalAnd( e.getVolumeXY(), bbExst, bbFlat, omNew.getBinaryValuesByte(), omFlat.getBinaryValuesByte() )) {
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
		}
		
		return writtenOneSlice;
	}
	
	private static boolean extendLow( ByteBuffer bbFlat, Extent e, ObjMask omNew, ObjMask omFlat, int zLow, int zCent, int zHigh ) {
		
		boolean andMode = true;
		boolean writtenOneSlice = false;
		
		// We go downwards
		for( int z=(zCent-1); z>=zLow;z-- ) {
			
			int zRel = z - zLow;
			
			// We want to set to the Flat version ANDed with
			ByteBuffer bbExst = omNew.getVoxelBox().getPixelsForPlane(zRel).buffer();
			
			if (andMode) {
			
				if (bufferLogicalAnd( e.getVolumeXY(), bbExst, bbFlat, omNew.getBinaryValuesByte(), omFlat.getBinaryValuesByte() )) {
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
		}
		return writtenOneSlice;
	}
		
	private static void setBufferLow( int numVoxels, ByteBuffer buffer, BinaryValuesByte bvb ) {
		for( int i=0; i<numVoxels; i++) {
			buffer.put(i, bvb.getOffByte());
		}
	}
	
	// Does a logicalAND between buffer and receive.  The result is placed in buffer.  receive is unchanged
	// Returns TRUE if at least one pixel is HIGH, or false otherwise
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
}
