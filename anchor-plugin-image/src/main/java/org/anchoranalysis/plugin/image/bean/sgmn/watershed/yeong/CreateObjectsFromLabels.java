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
import java.nio.IntBuffer;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

class CreateObjectsFromLabels {
	
	private CreateObjectsFromLabels() {}

	public static ObjMaskCollection apply( VoxelBox<IntBuffer> matS, Optional<ObjMask> mask) {
		
		BoundingBoxMap bbm = new BoundingBoxMap();
		
		if (mask.isPresent()) {
			updateBufferWithMask(matS, bbm, mask.get());
		} else {
			updateBufferWithoutMask(matS, bbm);
		}
		
		return bbm.deriveObjects(matS);
	}
	
	private static void updateBufferWithoutMask( VoxelBox<IntBuffer> matS, BoundingBoxMap bbm ) {
		
		Extent e = matS.extnt();
				
		for (int z=0; z<e.getZ(); z++) {
		
			// We iterate through each pixel in matS and replace it with the Index for the final object
			//   and also add the pixel to appropriate bounding box
			IntBuffer bbS = matS.getPixelsForPlane(z).buffer();
			
			int indxBuffer = 0;
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					putByte(indxBuffer, bbS, bbm, x, y, z);
					indxBuffer++;
				}
			}
		}
	}
	
	private static void updateBufferWithMask( VoxelBox<IntBuffer> matS, BoundingBoxMap bbm, ObjMask mask ) {
		
		Extent e = matS.extnt();
		
		// We iterate through each pixel in matS and replace it with the Index for the final object
		//   and also add the pixel to appropriate bounding box

		byte maskOn = mask.getBinaryValuesByte().getOnByte();
		
		Point3i crnrMin = mask.getBoundingBox().getCrnrMin();
		Point3i crnrMax = mask.getBoundingBox().calcCrnrMax();
		
		for (int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			IntBuffer bbS = matS.getPixelsForPlane(z).buffer();
			
			ByteBuffer bbMask = mask.getVoxelBox().getPixelsForPlane(z-crnrMin.getZ()).buffer();
		
			int indexMask = 0;
			for (int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for (int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
	
					int indxBuffer = e.offset(x, y);
					
					if( bbMask.get(indexMask)==maskOn) {
						putByte(indxBuffer, bbS, bbm, x, y, z);
					} else {
						bbS.put(indxBuffer,0);
					}
					
					indexMask++;
				}
			}
		}
	}
	
	private static void putByte(int indxBuffer, IntBuffer bbS, BoundingBoxMap bbm, int x, int y, int z) {
		int crntVal = bbS.get(indxBuffer);
		
		int outVal = bbm.addPointForValue(x, y, z, crntVal) + 1;
		
		bbS.put(indxBuffer, outVal);
	}
}