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

	public static BoundingBox boxFor( float[][] geometryArrs, int index, Point2i offset) {
		
		Point2f endUnrotated = new Point2f(
			geometryArrs[1][index],
			geometryArrs[2][index]
		);
		
		// Clockwise angle
		float angle = geometryArrs[4][index]; 
		
		return boxFor(
			geometryArrs[0][index],
			endUnrotated,
			geometryArrs[3][index],
			angle,
			offset
		);
	}
	
	private static BoundingBox boxFor( float p0, Point2f endUnrotated, float p3, float angle, Point2i offset) {
		
		// Width and height of bounding box
		float height = p0 + endUnrotated.getY();
		float width = endUnrotated.getX() + p3;
		
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
