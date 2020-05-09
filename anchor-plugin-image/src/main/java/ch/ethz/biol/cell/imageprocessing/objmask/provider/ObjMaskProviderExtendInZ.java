package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Extends an object as much as it can within the z-slices of a containing object
public class ObjMaskProviderExtendInZ extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ObjMaskProvider objsContainer;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objsSource = objs.create();
		
		// To avoid changing the original
		ObjMaskCollection objs = new ObjMaskCollection();
		objs.addAll(objsSource);

		ObjMaskCollection out = new ObjMaskCollection();
		
		
			
		ObjMaskCollection containerObjs = objsContainer.create();
			
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( containerObjs, objs );
		
		// For each obj we extend it into its container
		for( ObjWithMatches owm : matchList ) {

//			if (owm.getMatches().size()!=1) {
//				throw new CreateException( String.format("Matches must be exactly 1. Instead, they are %d", owm.getMatches().size() ));
//			}
			
			for( ObjMask omOther : owm.getMatches() ) {
				out.add( createExtendedObjMask(omOther, owm.getSourceObj() ) );
			}
		}
		
		return out;
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
	
	private static void setBufferLow( int numVoxels, ByteBuffer buffer, BinaryValuesByte bvb ) {
		for( int i=0; i<numVoxels; i++) {
			buffer.put(i, bvb.getOffByte());
		}
	}
	
	
	
	private boolean extendHigh( ByteBuffer bbFlat, Extent e, ObjMask omNew, ObjMask omFlat, int zLow, int zCent, int zHigh ) {
		
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
	
	
	
	private boolean extendLow( ByteBuffer bbFlat, Extent e, ObjMask omNew, ObjMask omFlat, int zLow, int zCent, int zHigh ) {
		
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
	
	
	private ObjMask createExtendedObjMask( ObjMask om, ObjMask container ) throws CreateException {
		
		ObjMask omFlat = om.flattenZ();
		
		int zLow = container.getBoundingBox().getCrnrMin().getZ();
		int zHigh = container.getBoundingBox().calcCrnrMax().getZ();
		
		int zCent = (int) om.centerOfGravity().getZ();
		
		
		//System.out.printf("Object: %s   zLow, zCent, zHigh = [%d,%d,%d]\n", om.centerOfGravity(), zLow, zCent, zHigh );
		
		Extent e = new Extent( omFlat.getBoundingBox().extnt().getX(), omFlat.getBoundingBox().extnt().getY(), zHigh-zLow+1 );
		Point3i crnrMin = new Point3i( omFlat.getBoundingBox().getCrnrMin().getX(), omFlat.getBoundingBox().getCrnrMin().getY(), zLow );
		BoundingBox BoundingBox = new BoundingBox( crnrMin, e );
		
		BoundingBox.intersect(container.getBoundingBox(), true);
		
		// We update these values after our intersection with the container, in case they have changed
		e = BoundingBox.extnt();
		
		
		// see bluelagoon/New-02(230)/000_DAPI'
		assert (container.getBoundingBox().contains(BoundingBox));
		
		ObjMask omNew = container.createSubmaskAlwaysNew(BoundingBox);
				
		ByteBuffer bbFlat = omFlat.getVoxelBox().getPixelsForPlane(0).buffer();
		
		
		zLow = BoundingBox.getCrnrMin().getZ();
		zHigh = BoundingBox.calcCrnrMax().getZ();
		
		if (zCent>zHigh) { zCent = zHigh; }
		if (zCent<zLow) { zCent = zLow; }
		
		//zCent = ((int) omNew.centerOfGravity().z);
		extendHigh( bbFlat, e, omNew, omFlat, zLow, zCent, zHigh );
		extendLow( bbFlat, e, omNew, omFlat, zLow, zCent, zHigh );

		return omNew; 
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}


	public ObjMaskProvider getObjsContainer() {
		return objsContainer;
	}


	public void setObjsContainer(ObjMaskProvider objsContainer) {
		this.objsContainer = objsContainer;
	}


	// Merges small obj mask provider objets togehter
}
