package ch.ethz.biol.cell.sgmn.objmask;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.PointRange;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Maximum intensity projection and scales down
public class ObjMaskChnlUtilities {

	public static ObjMaskCollection calcObjMaskFromLabelChnl(
		VoxelBox<ByteBuffer> bufferAcccess,
		int maxColorID,
		int minBBoxVolume
	) {
		int numPixel[] = new int[maxColorID];
		List<BoundingBox> bboxList = calcBBox( bufferAcccess, maxColorID, numPixel );
		return calcObjMask( bboxList, bufferAcccess, minBBoxVolume );
	}
	
	
	private static ObjMaskCollection calcObjMask(
		List<BoundingBox> bboxList,
		VoxelBox<ByteBuffer> bufferAccess,
		int smallVolumeThreshold
	) {
		
		ObjMaskCollection list = new ObjMaskCollection();
		
		int col = 0;
		for (BoundingBox bbox : bboxList) {
			col++;
			
			if ( bbox.extnt().getVolumeXY() < smallVolumeThreshold ) {
				continue;
			}
			
			list.add(
				bufferAccess.equalMask(bbox, col)
			);
		}
				
		return list;
	}
	
	// TODO Optimize by requiring sorted list of points and moving through the z-stacks sequentially
	public static ObjMask createObjMaskFromPoints( List<Point3i> points ) throws CreateException {
		
		try {
			ObjMask om = new ObjMask(
				BoundingBoxFromPoints.forList(points)
			);
			
			for( int i=0; i<points.size(); i++) {
				
				Point3i pnt = points.get(i);
				
				int relX = pnt.getX()-om.getBoundingBox().getCrnrMin().getX();
				int relY = pnt.getY()-om.getBoundingBox().getCrnrMin().getY();
				int relZ = pnt.getZ()-om.getBoundingBox().getCrnrMin().getZ();
				
				ByteBuffer bb = om.getVoxelBox().getPixelsForPlane(relZ).buffer();
				bb.put( om.getVoxelBox().extnt().offset(relX, relY), om.getBinaryValuesByte().getOnByte() );
			}
			
			return om;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	
	private static List<BoundingBox> calcBBox( VoxelBox<ByteBuffer> bufferAccess, int numC, int[] numPixel ) {
		
		List<PointRange> list = new ArrayList<>(numC);
		
		for (int i=1; i<=numC; i++) {
			list.add(	new PointRange() );
		}
		
		for (int z=0; z<bufferAccess.getPlaneAccess().extnt().getZ(); z++) {
			
			ByteBuffer pixel = bufferAccess.getPlaneAccess().getPixelsForPlane(z).buffer();
			
			for (int y=0; y<bufferAccess.getPlaneAccess().extnt().getY(); y++) {
				for (int x=0; x<bufferAccess.getPlaneAccess().extnt().getX(); x++) {
					
					int col = ByteConverter.unsignedByteToInt(pixel.get());
					
					if (col==0) {
						continue;
					}
					
					list.get(col-1).add( x,y,z );
					numPixel[col-1]++;
				}
			}
		}

		/** Convert to bounding-boxes after filtering any empty point-ranges */
		return list
			.stream()
			.filter( p -> !p.isEmpty() )
			.map(PointRange::deriveBoundingBoxNoCheck)
			.collect(Collectors.toList());
	}
	


}
