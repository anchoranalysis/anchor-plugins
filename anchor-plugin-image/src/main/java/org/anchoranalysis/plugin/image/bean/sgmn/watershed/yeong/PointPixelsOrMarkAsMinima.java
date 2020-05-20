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
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

class PointPixelsOrMarkAsMinima {
	
	private PointPixelsOrMarkAsMinima() {}
		
	public static void apply( VoxelBox<?> vbImg, EncodedVoxelBox matS, Optional<ObjMask> mask, Optional<MinimaStore> minimaStore ) {
		
		SlidingBufferPlus state = new SlidingBufferPlus(vbImg, matS, mask, minimaStore);
		
		if (mask.isPresent()) {
			slideWithMask(state, mask.get());
		} else {
			slide(state);
		}
	}
	
	private static void slide( SlidingBufferPlus buffer) {
		
		Extent e = buffer.extnt();
		buffer.init(0);
		
		for (int z=0; z<e.getZ(); z++) {
			
			EncodedIntBuffer bbS = buffer.getSPlane(z);
			int indxBuffer = 0;
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					if (bbS.isUnvisited(indxBuffer)) {
						buffer.visitPixel( indxBuffer, x, y, z, bbS);
					}

					indxBuffer++;
				}
			}
			
			buffer.shift();
		}
	}

	private static void slideWithMask( SlidingBufferPlus buffer, ObjMask om ) {
			
		Point3i crnrMin = om.getBoundingBox().getCrnrMin();
		byte maskOn = om.getBinaryValuesByte().getOnByte();
		Extent eObjMask = om.getVoxelBox().extnt();

		Extent e = buffer.extnt();
		buffer.init(crnrMin.getZ());

		for (int z=0; z<eObjMask.getZ(); z++) {
		
			int z1 = z+crnrMin.getZ();
			EncodedIntBuffer bbS = buffer.getSPlane(z1);
			
			ByteBuffer bbOM = om.getVoxelBox().getPixelsForPlane(z).buffer();
	
			for (int y=0; y<eObjMask.getY(); y++) {
				for (int x=0; x<eObjMask.getX(); x++) {
			
					int offsetObjMask = eObjMask.offset(x, y);

					if (bbOM.get(offsetObjMask)==maskOn) {

						int x1 = x+crnrMin.getX();
						int y1 = y+crnrMin.getY();
						
						int indxBuffer = e.offset(x1, y1);
						
						if (bbS.isUnvisited(indxBuffer)) {
							buffer.visitPixel( indxBuffer, x1, y1, z1, bbS );
						}
					}
				}
			}
			
			buffer.shift();
		}
	}
}