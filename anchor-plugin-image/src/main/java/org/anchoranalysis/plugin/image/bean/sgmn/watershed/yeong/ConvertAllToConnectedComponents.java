package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

// Converts all the minima and direction IDs into the connected components they represent
class ConvertAllToConnectedComponents {

	public void doForAll( EncodedVoxelBox matS ) {
		
		Extent e = matS.extnt();
		
		int indxGlobal = 0;
		
		for (int z=0; z<e.getZ(); z++) {
			
			int indxBuffer = 0;

			// For 3d we need to translate the global index back to local
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z);
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					int crntVal = bbS.getCode(indxBuffer);
					
					assert( !matS.isPlateau(crntVal) );
					assert( !matS.isUnvisited(crntVal) );
					assert( !matS.isTemporary(crntVal) );
					
					// We translate the value into directions and use that to determine where to
					//   travel to
					if ( matS.isMinima(crntVal) ) {
						bbS.putConnectedComponentID(indxBuffer, indxGlobal);
						
						// We maintain a mapping between each minimas indxGlobal and 
					} else if (matS.isConnectedComponentIDCode(crntVal)) {
						// NO CHANGE
					} else {
						int finalIndex = matS.calculateConnectedComponentID(x, y, z, crntVal);
						bbS.putCode(indxBuffer, finalIndex );
					}
					
					indxGlobal++;
					indxBuffer++;
				}
			}
		}
	}
	
	
	
	public void doForMask( EncodedVoxelBox matS, ObjMask om ) {
		
		Extent e = matS.extnt();
		
		Extent eObjMask = om.getVoxelBox().extnt();
		Point3i crnrMin = om.getBoundingBox().getCrnrMin();
		
		byte valueOn = BinaryValuesByte.getDefault().getOnByte();
		
		

		// Just checks that we have no temporaries
		for (int z=0; z<eObjMask.getZ(); z++) {

			// For 3d we need to translate the global index back to local
			int z1 = z + crnrMin.getZ();
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z1);
			
			ByteBuffer bbOM = om.getVoxelBox().getPixelsForPlane(z).buffer();
			
			for (int y=0; y<eObjMask.getY(); y++) {
				for (int x=0; x<eObjMask.getX(); x++) {

					if (bbOM.get()==valueOn) {
						int x1 = x + crnrMin.getX();
						int y1 = y + crnrMin.getY();

						int indxBuffer = e.offset(x1, y1);
						
						int crntVal = bbS.getCode(indxBuffer);
						
						assert( !matS.isPlateau(crntVal) );
						assert( !matS.isUnvisited(crntVal) );
						assert( !matS.isTemporary(crntVal) );
					}
				}
			}
		}
		
		
		for (int z=0; z<eObjMask.getZ(); z++) {

			// For 3d we need to translate the global index back to local
			int z1 = z + crnrMin.getZ();
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z1);
			
			ByteBuffer bbOM = om.getVoxelBox().getPixelsForPlane(z).buffer();
			
			for (int y=0; y<eObjMask.getY(); y++) {
				for (int x=0; x<eObjMask.getX(); x++) {

					if (bbOM.get()==valueOn) {
						int x1 = x + crnrMin.getX();
						int y1 = y + crnrMin.getY();

						int indxBuffer = e.offset(x1, y1);
						
						int crntVal = bbS.getCode(indxBuffer);
						
						assert( !matS.isPlateau(crntVal) );
						assert( !matS.isUnvisited(crntVal) );
						assert( !matS.isTemporary(crntVal) );
						
						// We translate the value into directions and use that to determine where to
						//   travel to
						if ( matS.isMinima(crntVal) ) {
							
							int indxGlobal = e.offset(x1, y1, z1);
							bbS.putConnectedComponentID(indxBuffer, indxGlobal);
							
							// We maintain a mapping between each minimas indxGlobal and 
						} else if (matS.isConnectedComponentIDCode(crntVal)) {
							// NO CHANGE
						} else {
							int finalIndex = matS.calculateConnectedComponentID(x1, y1, z1, crntVal);
							bbS.putCode(indxBuffer, finalIndex );
						}
					}
				}
			}
		}
	}
	
	
}