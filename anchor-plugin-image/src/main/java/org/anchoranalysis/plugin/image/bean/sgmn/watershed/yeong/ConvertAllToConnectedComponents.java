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
import java.util.Optional;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

// Converts all the minima and direction IDs into the connected components they represent
class ConvertAllToConnectedComponents {

	private ConvertAllToConnectedComponents() {}
	
	public static void apply( EncodedVoxelBox matS, Optional<ObjMask> mask ) {
		if (mask.isPresent()) {
			applyWithMask(matS, mask.get());
		} else {
			applyWithoutMask(matS);
		}
	}
	
	private static void applyWithoutMask( EncodedVoxelBox matS ) {
		
		Extent e = matS.extnt();
		
		int indxGlobal = 0;
		
		for (int z=0; z<e.getZ(); z++) {
			
			int indxBuffer = 0;

			// For 3d we need to translate the global index back to local
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z);
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					bbS.convertCode(
						indxBuffer,
						indxGlobal,
						matS,
						x,
						y,
						z
					);
					
					indxGlobal++;
					indxBuffer++;
				}
			}
		}
	}
		
	private static void applyWithMask( EncodedVoxelBox matS, ObjMask om ) {
		
		Extent e = matS.extnt();
		Extent eObjMask = om.getVoxelBox().extnt();
		Point3i crnrMin = om.getBoundingBox().getCrnrMin();
		byte valueOn = om.getBinaryValuesByte().getOnByte();
		
		for (int z=0; z<eObjMask.getZ(); z++) {

			// For 3d we need to translate the global index back to local
			int z1 = z + crnrMin.getZ();
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z1);
			
			ByteBuffer bbOM = om.getVoxelBox().getPixelsForPlane(z).buffer();
			
			int zOffset = e.offset(0, 0, z1);
			
			for (int y=0; y<eObjMask.getY(); y++) {
				for (int x=0; x<eObjMask.getX(); x++) {

					if (bbOM.get()==valueOn) {
						int x1 = x + crnrMin.getX();
						int y1 = y + crnrMin.getY();
 
						int offset = e.offset(x1, y1); 
						bbS.convertCode(
							offset,
							zOffset + offset,
							matS,
							x1,
							y1,
							z1
						);
					}
				}
			}
		}
	}
}