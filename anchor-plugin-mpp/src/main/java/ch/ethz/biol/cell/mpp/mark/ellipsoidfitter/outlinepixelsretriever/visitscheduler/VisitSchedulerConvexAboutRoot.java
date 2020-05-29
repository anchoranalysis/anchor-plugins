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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

@SuppressWarnings("unused")
public class VisitSchedulerConvexAboutRoot extends VisitScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Point3i root;
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		return null;
	}

	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {

	}

	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) throws InitException {
		this.root = root;
	}

	private static Point3d relVector( Point3i root, Point3i pnt ) {
		// Calculate angle from root to pnt
		Point3d rootSub = PointConverter.doubleFromInt(root);
		
		Point3d pntNew = PointConverter.doubleFromInt(pnt);
		pntNew.sub(rootSub);
		
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
			mag = 1 / mag;
			dist.scale( mag );
		}

		Point3d destPntD = new Point3d(destPnt.getX(),destPnt.getY(),destPnt.getZ());
		
		// Now we keep checking that points are inside the mask until we reach our final point
		Point3i p = new Point3i(root);
		Point3d pD = new Point3d(root.getX(),root.getY(),root.getZ());
		while (!pointEquals(pD,destPntD)) {
			p.setX( (int) Math.floor(pD.getX()) );
			p.setY( (int) Math.floor(pD.getY()) );
			p.setZ( (int) Math.floor(pD.getZ()) );
			
			if (debug) {
				System.out.printf("%s ", pD.toString());
			}
			
			if (!isPntOnObj(p, bvb.getVoxelBox(), bvb.getBinaryValues() )) {
				
				if (debug) {
					System.out.printf("failed%n%n");
				}
				return false;
			}
			
			pD.setX( pD.getX() + dist.getX() );
			pD.setY( pD.getY() + dist.getY() );
			pD.setZ( pD.getZ() + dist.getZ() );
		} 
		
		return true;
	}
	
	@Override
	public boolean considerVisit(Point3i pnt, int distAlongContour,
			ObjMask objMask) {
		return isPointConvexTo(root, pnt, objMask.binaryVoxelBox());
	}
	
	private static boolean pointEquals( Point3d p1, Point3d p2) {
		return (p1.distanceSquared(p2)<1.0);
		// If the distance between the two point
		//return (p1.x==p2.x) && (p1.y==p2.y)&& (p1.z==p2.z); 
	}
	
	private static boolean isPntOnObj( Point3i pnt, VoxelBox<ByteBuffer> vb, BinaryValues bv ) {
		
		if (!vb.extent().contains(pnt)) {
			return false;
		}
		
		return vb.getVoxel(pnt.getX(), pnt.getY(), pnt.getZ())==bv.getOnInt();
	}
	
}
