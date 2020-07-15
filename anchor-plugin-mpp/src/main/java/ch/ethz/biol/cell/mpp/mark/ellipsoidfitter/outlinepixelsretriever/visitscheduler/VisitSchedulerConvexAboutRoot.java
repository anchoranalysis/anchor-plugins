package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

/*
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class VisitSchedulerConvexAboutRoot extends VisitScheduler {

	private Point3i root;
	
	@Override
	public Optional<Tuple3i> maxDistFromRootPoint(ImageResolution res) {
		return Optional.empty();
	}

	@Override
	public void beforeCreateObject(RandomNumberGenerator re, ImageResolution res)
			throws InitException {
		// NOTHING TO DO
	}

	@Override
	public void afterCreateObject(Point3i root, ImageResolution res, RandomNumberGenerator re) throws InitException {
		this.root = root;
	}

	private static Point3d relVector( Point3i root, Point3i pnt ) {
		// Calculate angle from root to pnt
		Point3d rootSub = PointConverter.doubleFromInt(root);
		
		Point3d pntNew = PointConverter.doubleFromInt(pnt);
		pntNew.subtract(rootSub);
		return pntNew;
	}
	
	public static boolean isPointConvexTo( Point3i root, Point3i pnt, BinaryVoxelBox<ByteBuffer> bvb ) {
		return isPointConvexTo(root, pnt, bvb, false);
	}
	
	public static boolean isPointConvexTo( Point3i root, Point3i destPnt, BinaryVoxelBox<ByteBuffer> bvb, boolean debug ) {
		
		Point3d dist = relVector(root, destPnt);
		double mag = Math.pow(
			Math.pow(dist.getX(), 2) + Math.pow(dist.getY(), 2) + Math.pow(dist.getZ(), 2),
			0.5
		); 
		
		if (mag>0.0) {
			dist.scale( 1/mag );
		}
		
		// Now we keep checking that points are inside the mask until we reach our final point
		Point3d pnt = PointConverter.doubleFromInt(root);
		while (!pointEquals(pnt,destPnt)) {
			
			if (debug) {
				System.out.printf("%s ", pnt.toString());		// NOSONAR
			}

			if (!isPntOnObj(pnt, bvb.getVoxelBox(), bvb.getBinaryValues() )) {
				
				if (debug) {
					System.out.printf("failed%n%n");			// NOSONAR
				}
				return false;
			}
			
			pnt.increment(dist);
		} 
		
		return true;
	}
	
	@Override
	public boolean considerVisit(Point3i pnt, int distAlongContour,	ObjectMask object) {
		return isPointConvexTo(root, pnt, object.binaryVoxelBox());
	}
	
	private static boolean pointEquals( Point3d point1, Point3i point2) {
		return point1.distanceSquared(point2) < 1.0;
	}
	
	private static boolean isPntOnObj( Point3d pnt, VoxelBox<ByteBuffer> vb, BinaryValues bv ) {
		
		Point3i pntInt = PointConverter.intFromDouble(pnt);
		
		if (!vb.extent().contains(pntInt)) {
			return false;
		}
		
		return vb.getVoxel(pntInt)==bv.getOnInt();
	}
	
}
