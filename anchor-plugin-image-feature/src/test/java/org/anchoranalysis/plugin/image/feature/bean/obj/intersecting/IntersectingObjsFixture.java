package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.test.feature.plugins.CircleObjMaskFixture;

public class IntersectingObjsFixture {

	private static final int INITIAL_MARGIN = 5;
	
	private static final int INITIAL_RADIUS = 5;
	
	private static final int RADIUS_INCR = 2;
	
	/**
	 *  Generates a number of circles that intersect and don't intersect
	 * 
	 *  @param numIntersecting the number of circles that intersect that should be produced
	 *  @param numNotIntersecting the number of circles that do not intersect that should be produced
	 *  @param boolean sameSize iff TRUE all circles have the same radius (INITIAL_RAIDUS), otherwise the radius gradually increments
	 * */
	public static ObjMaskCollection generateIntersectingObjs( int numIntersecting, int numNotIntersecting, boolean sameSize ) {
	
		int radius = INITIAL_RADIUS;
		
		ObjMaskCollection out = new ObjMaskCollection();
	
		Point2i center = new Point2i( INITIAL_MARGIN + radius, INITIAL_MARGIN + radius);

		// Keep on generating circles of radius 10 with centers radius*1.5 apart, so that they intersect
		for( int i=0; i<numIntersecting; i++) {
			out.add(
				generateCircleAndShift(center, radius, 1.5)
			);
			if (!sameSize) {
				radius += RADIUS_INCR;
			}
		}
		
		// Now generate at radius 3 apartment, so that they do not intersect
		for( int i=0; i<numNotIntersecting; i++) {
			out.add(
				generateCircleAndShift(center, radius, 3)
			);
			
			if (!sameSize) {
				radius += RADIUS_INCR;
			}
		}
		
		// Make sure we haven't generated so many we've run out of the scene
		assert( CircleObjMaskFixture.sceneContains(center) );
		
		return out;
	}
	
	private static ObjMask generateCircleAndShift(Point2i center, int radius, double factor) {
		ObjMask om = CircleObjMaskFixture.circleAt(center, radius);
		
		int shift = (int) (factor*radius);
		center.incrX(shift);
		center.incrY(shift);
		
		return om;
	}
}
