package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*
 * #%L
 * anchor-plugin-points
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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryVoxelBox;

// Strongly influenced by http://rsb.info.nih.gov/ij/macros/ConvexHull.txt
public class ConvexHullUtilities {

	/**
	 * Extract points from the outline and maybe apply a convex-hull to filter them
	 * 
	 * @param obj object to extract points from
	 * @param minNumPnts a minimum of number of points (before ant convex hull filtering) that must be found, otherwise no list is returned at all
	 * @param applyConvexHull iff TRUE the points are filtered by the convex-hull operation
	 * @return a list of the points iif the minimum threshold is fulfilled
	 * @throws CreateException
	 */
	public static Optional<List<Point2i>> extractPointsFromOutline( ObjectMask obj, int minNumPnts, boolean applyConvexHull ) throws CreateException {

		List<Point2i> pts = new ArrayList<>();
		addPointsFromObjOutline(obj, pts);
		
		if (pts.size()<minNumPnts) {
			return Optional.empty();
		}
		
		return applyConvexHull ? convexHull2D(pts) : Optional.of(pts);
	}

	public static Optional<List<Point2i>> convexHullFromAllOutlines( ObjectCollection objs, int minNumPnts ) throws CreateException {
		
		List<Point2i> pts = new ArrayList<>();
			
		for( ObjectMask om : objs ) {
			addPointsFromObjOutline(om, pts);
		}
		
		if (pts.size()<minNumPnts) {
			return Optional.empty();
		}
		
		return convexHull2D(pts);
	}
	
	// Gift wrap algorithm
	public static Optional<List<Point2i>> convexHull2D( List<Point2i> pntsIn ) {
		
		if (pntsIn.size()==0) {
			return Optional.of(
				new ArrayList<>()
			);
		}
		
		int xbase = 0;
		int ybase = 0;
		
		int[] xx = new int[pntsIn.size()];
		int[] yy = new int[pntsIn.size()];
		int n2 = 0;
		
		int p1 = calculateStartingIndex( pntsIn );
		
		
		int pstart = p1;
		int x1, y1, x2, y2, x3, y3, p2, p3;
		
		int count = 0;
		do {
			x1 = pntsIn.get(p1).getX();
			y1 = pntsIn.get(p1).getY();
			p2 = p1+1;
			
			if (p2==pntsIn.size()) {
				p2=0;
			}
			
			x2 = pntsIn.get(p2).getX();
			y2 = pntsIn.get(p2).getY();
			p3 = p2+1;
			
			if (p3==pntsIn.size()) {
				p3=0;
			}
		
			int determinate;
			do {
				x3 = pntsIn.get(p3).getX();
				y3 = pntsIn.get(p3).getY();
				determinate = x1*(y2-y3)-y1*(x2-x3)+(y3*x2-y2*x3);
				if (determinate>0)
					{x2=x3; y2=y3; p2=p3;}
				p3 += 1;
				if (p3==pntsIn.size()) p3 = 0;
			} while (p3!=p1);
			
			if (n2<pntsIn.size()) { 
				xx[n2] = xbase + x1;
				yy[n2] = ybase + y1;
				n2++;
			} else {
				count++;
				if (count>10) {
					return Optional.empty();
				}
			}
			
			p1 = p2;
			
		} while (p1!=pstart);
		
		
		
		// n2 is number of points out
		List<Point2i> out = new ArrayList<>();
		for( int i=0; i<n2; i++) {
			out.add( new Point2i(xx[i], yy[i]) );
		}

		return Optional.of(out);
	}
	
	private static int calculateStartingIndex( List<Point2i> pntsIn ) {
		
		int smallestY = Integer.MAX_VALUE;
		int x, y;
		for (int i=0; i<pntsIn.size(); i++) {
			y = pntsIn.get(i).getY();
			if (y<smallestY)
			smallestY = y;
		}
		
		int smallestX = Integer.MAX_VALUE;
		int p1 = 0;
		for (int i=0; i<pntsIn.size(); i++) {
			x = pntsIn.get(i).getX();
			y = pntsIn.get(i).getY();
			if (y==smallestY && x<smallestX) {
				smallestX = x;
				p1 = i;
			}
		}
		return p1;
	}
		
	public static void addPointsFromObjOutline( ObjectMask obj, List<Point2i> pts) throws CreateException {
		ObjectMask outline = FindOutline.outline(obj, 1, true, false);
		PointsFromBinaryVoxelBox.addPointsFromVoxelBox(
			outline.binaryVoxelBox(),
			outline.getBoundingBox().getCrnrMin(),
			pts
		);
	}
	
}
