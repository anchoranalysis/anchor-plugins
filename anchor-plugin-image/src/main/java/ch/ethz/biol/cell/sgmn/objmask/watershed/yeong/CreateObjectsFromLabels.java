package ch.ethz.biol.cell.sgmn.objmask.watershed.yeong;

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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

class CreateObjectsFromLabels {

	public static ObjMaskCollection doForAll( VoxelBox<IntBuffer> matS ) {
		
		Extent e = matS.extnt();
		
		BoundingBoxMap bbm = new BoundingBoxMap();
		
		for (int z=0; z<e.getZ(); z++) {
		
			// We iterate through each pixel in matS and replace it with the Index for the final object
			//   and also add the pixel to appropriate bounding box
			IntBuffer bbS = matS.getPixelsForPlane(z).buffer();
			
			int indxBuffer = 0;
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
	
					int crntVal = bbS.get(indxBuffer);
					
					//assert( matM.getPixelsForPlane(0).get(crntVal)==ObjMask.VALUE_ON );
					int reorderedIndex = bbm.indexForValue(crntVal);
					BoundingBox BoundingBox = bbm.getBBoxForIndx(reorderedIndex, x, y, z);
					BoundingBox.add(x,y,z);
					
					bbS.put(indxBuffer,reorderedIndex+1);
					
					indxBuffer++;
				}
			}
		}

		return objMaskCollectionFromMask(matS, bbm.getList());
	}
	
	
	public static ObjMaskCollection doForMask( VoxelBox<IntBuffer> matS, ObjMask om ) {
		
		Extent e = matS.extnt();
		
		BoundingBoxMap bbm = new BoundingBoxMap();
		
		// We iterate through each pixel in matS and replace it with the Index for the final object
		//   and also add the pixel to appropriate bounding box

		byte maskOn = om.getBinaryValuesByte().getOnByte();
		
		Point3i crnrMin = om.getBoundingBox().getCrnrMin();
		Point3i crnrMax = om.getBoundingBox().calcCrnrMax();
		
		for (int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			IntBuffer bbS = matS.getPixelsForPlane(z).buffer();
			
			ByteBuffer bbMask = om.getVoxelBox().getPixelsForPlane(z-crnrMin.getZ()).buffer();
		
			int indexMask = 0;
			for (int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for (int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
	
					int indxBuffer = e.offset(x, y);
					
					if( bbMask.get(indexMask)==maskOn) {
						
						
						int crntVal = bbS.get(indxBuffer);
						
						//assert( matM.getPixelsForPlane(0).get(crntVal)==ObjMask.VALUE_ON );
						int reorderedIndex = bbm.indexForValue(crntVal);
						BoundingBox BoundingBox = bbm.getBBoxForIndx(reorderedIndex, x, y, z);
						BoundingBox.add(x,y,z);
						
						bbS.put(indxBuffer,reorderedIndex+1);
					} else {
						bbS.put(indxBuffer,0);
					}
					
					indexMask++;
				}
			}
		}

		return objMaskCollectionFromMask(matS, bbm.getList());
	}

	private static ObjMaskCollection objMaskCollectionFromMask(
		VoxelBox<IntBuffer> matS,
		List<BoundingBox> bboxList
	) {
		
		ObjMaskCollection out = new ObjMaskCollection();
		for (int i=0; i<bboxList.size(); i++) {
			
			BoundingBox bbox = bboxList.get(i);
			
			if(bbox==null) {
				continue;
			}
			
			ObjMask om = matS.equalMask(bbox, i+1 );
			
			//int countR = om.getVoxelBox().countEqual( ObjMask.VALUE_ON_INT );
			//System.out.printf("Countequal=%d  %f\n", countR, ((double) countR) / om.getBoundingBox().getExtnt().getVolume() );
			
			out.add(om);
		}
		return out;
	}
}