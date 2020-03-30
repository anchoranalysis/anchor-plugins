package org.anchoranalysis.plugin.opencv.bean.text;

import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point2f;
import org.anchoranalysis.core.geometry.Point2i;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.BoundingBox;

/**
 * Extracts a bounding box from arrays with different co-oridinates
 * 
 * @author owen
 *
 */
class BoundingBoxFromArrays {

	/**
	 * Builds a bounding-box from the arrays returned from the EAST algorithm
	 * 
	 * <p>The bounding-boxes are encoded in RBOX format, see the original EAST paper
	 * Zhou et al. 2017</p>
	 * 
	 * @param geometryArrs an array of 5 arrays
	 * @param index the current index to look in each of the 5 arrays
	 * @param offset an offset in the scene to add to each generated bounding-box
	 * @return a bounding-box
	 */
	public static BoundingBox boxFor( float[][] geometryArrs, int index, Point2i offset) {
		
		Point2f startUnrotated = new Point2f(
			geometryArrs[3][index],  // left-boundary-rectangle (x-min)
			geometryArrs[0][index] 	 // top-boundary-rectangle (y-min)
		);
		
		Point2f endUnrotated = new Point2f(
			geometryArrs[1][index], // right-boundary-rectangle (x-max)
			geometryArrs[2][index]  // bottom-boundary-rectangle (y-max)
		);
		
		// Clockwise angle
		float angle = geometryArrs[4][index]; 
		
		return boxFor(
			startUnrotated,
			endUnrotated,
			angle,
			offset
		);
	}
	
	private static BoundingBox boxFor( Point2f startUnrotated, Point2f endUnrotated, float angle, Point2i offset) {
		
		// Width and height of bounding box
		float height = startUnrotated.getY() + endUnrotated.getY();
		float width = endUnrotated.getX() + startUnrotated.getX();
		
		Point2d endRotated = rotateClockwiseAboutAngle(endUnrotated, angle);
		endRotated.add(offset);
		
		return boxFor(
			PointConverter.intFromDouble(endRotated),
			(int) width,
			(int) height
		);
	}
		
	/** Create bounding box from the end-crnr and a width and height */
	private static BoundingBox boxFor( Point2i end, int width, int height) {

		// Force some width if 0
		if (width==0) {
			width=1;
		}
		
		// Force some height if 0
		if (height==0) {
			height=1;
		}
		
		int startX = end.getX() - width;
		int startY = end.getY() - height;
		
		assert( startX>=0 );
		assert( startY>=0 );
		
		return new BoundingBox(
			new Point3i(startX, startY, 0),
			new Point3i(end.getX(), end.getY(), 0)
		);
	}
	
	private static Point2d rotateClockwiseAboutAngle( Point2f pnt, float angle ) {
		
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		
		double xRot = + (cos * pnt.getX()) + (sin * pnt.getY());
		double yRot = - (sin * pnt.getX()) + (cos * pnt.getY());
		
		return new Point2d(xRot, yRot);
	}
}
