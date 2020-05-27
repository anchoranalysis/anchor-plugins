package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*-
 * #%L
 * anchor-plugin-ij
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

/** Traces out the shortest-path (a line) between two points in an object-mask.
 * 
 *  This ensures the two points are connected (4/6 neighbourhood)
 *  
 * @author feehano
 *
 */
class ObjMaskWalkShortestPath {

	/** Walks a 4-connected line between two points, producing a minimal object-mask for both */
	public static ObjMask walkLine( Point3d pnt1, Point3d pnt2 ) throws OperationFailedException {
		
		List<Point3i> list = new ArrayList<>();
		list.add( new Point3i(pnt1) );
		list.add( new Point3i(pnt2) );
		
		return walkLineInternal( list );
	}

	/** Joins a series of points together by drawling a line between each success pair of points */
	public static ObjMask walkLine( List<Point3d> pnts ) throws OperationFailedException {
		
		if (pnts.size()<2) {
			throw new OperationFailedException(
				String.format("A minimum of two points is requred to walk a line, but there are only %d points.", pnts.size())
			);
		}
		
		return walkLineInternal(
			PointConverter.convert3i(pnts)
		);
	}
	
	
	
	private static ObjMask walkLineInternal( List<Point3i> pnts ) throws OperationFailedException {
				
		checkCoplanar( pnts );
		
		BoundingBox bbox = BoundingBoxFromPoints.forList(pnts);
		
		ObjMask om = new ObjMask(bbox);
		
		for( int i=0; i<(pnts.size()-1); i++ )  {
		
			Point3i pnt1 = pnts.get(i);
			Point3i pnt2 = pnts.get(i+1);
			assert(pnt1.getZ()==pnt2.getZ());
			
			drawLineOnVoxelBuffer(
				om.binaryVoxelBox().getVoxelBox().getPixelsForPlane(pnt1.getZ() - bbox.getCrnrMin().getZ()),
				om.binaryVoxelBox().getVoxelBox().extent(),
				om.binaryVoxelBox().getBinaryValues().createByte(),
				pnt1,
				pnt2,
				bbox.getCrnrMin()
			);
		}
				
		return om;
		
	}
		
	private static void checkCoplanar( List<Point3i> pnts ) throws OperationFailedException {

		Point3i firstPnt = null;
		
		for( Point3i pnt : pnts ) {
			
			if (firstPnt==null) {
				firstPnt = pnt;
			}
			
			// Only accept points in 2D
			if (firstPnt.getZ()!=pnt.getZ()) {
				throw new OperationFailedException(
					String.format("the first point in the list (%d) and another pnt (%d) have different z values. This algorithm only supports co-planar points (parallel to XY-axis)", firstPnt.getZ(), pnt.getZ())
				);
			}
		}
	}
	
	private static void drawLineOnVoxelBuffer(
		VoxelBuffer<ByteBuffer> plane,
		Extent extnt,
		BinaryValuesByte bvb,
		Point3i pnt1,
		Point3i pnt2,
		ReadableTuple3i crnrMin
	) {
		drawLine4(
			plane,
			extnt,
			bvb,
			pnt1.getX() - crnrMin.getX(),
			pnt1.getY() - crnrMin.getY(),
			pnt2.getX() - crnrMin.getX(),
			pnt2.getY() - crnrMin.getY()
		);
	}
	
	/* 
	 * From ImageProcessor.java in ImageJ
	 * 
	 * Draws a line using the Bresenham's algorithm that is 4-connected instead of 8-connected.<br>
	Based on code from http://stackoverflow.com/questions/5186939/algorithm-for-drawing-a-4-connected-line<br>
	Author: Gabriel Landini (G.Landini at bham.ac.uk)
	 */
	 private static void drawLine4(VoxelBuffer<ByteBuffer> plane, Extent extnt, BinaryValuesByte bvb, int x1, int y1, int x2, int y2) {
				  
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sgnX = sgn(x1,x2);
		int sgnY = sgn(y1,y2);
		int e = 0;
		for (int i=0; i < dx+dy; i++) {
			drawPoint( plane, extnt, bvb, x1, y1 );

			int e1 = e + dy;
			int e2 = e - dx;
			if (Math.abs(e1) < Math.abs(e2)) {
				x1 += sgnX;
				e = e1;
			} else {
				y1 += sgnY;
				e = e2;
			}
		}
		drawPoint( plane, extnt, bvb, x2, y2 );
	}
	 
	 private static int sgn( int x1, int x2 ) {
		 return x1 < x2 ? 1 : -1;
	 }
	 
	 private static void drawPoint( VoxelBuffer<ByteBuffer> plane, Extent extnt, BinaryValuesByte bvb, int x, int y ) {
		 plane.putByte( extnt.offset(x,y) , bvb.getOnByte() );
	 }
}
