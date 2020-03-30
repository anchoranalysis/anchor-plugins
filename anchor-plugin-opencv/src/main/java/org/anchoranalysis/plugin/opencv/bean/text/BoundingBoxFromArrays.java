package org.anchoranalysis.plugin.opencv.bean.text;

import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point2f;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;

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

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;

/**
 * Extracts a bounding box from arrays returned by the EAST deep learning model.
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
	public static BoundingBox boxFor( float[][] geometryArrs, int index, Point2i offset, Extent extnt) {
		
		Point2f startUnrotated = new Point2f(
			geometryArrs[3][index],  // distance to left-boundary of box (x-min)
			geometryArrs[0][index] 	 // distance to top-boundary of box (y-min)
		);
		
		// To bring to the same relative-direction as endRotated
		startUnrotated.scale(-1);
		
		Point2f endUnrotated = new Point2f(
			geometryArrs[1][index],  // distance to right-boundary of box (x-max)
			geometryArrs[2][index]   // distance to bottom-boundary of box (y-max)
		);
		
		// Clockwise angle to rotate around the offset
		float angle = geometryArrs[4][index]; 
		
		return boxFor(
			startUnrotated,
			endUnrotated,
			angle,
			offset
		);
	}
	
	private static BoundingBox boxFor( Point2f startUnrotated, Point2f endUnrotated, float angle, Point2i offset) {
		
		Point2d startRotated = rotateClockwiseWithOffset(startUnrotated, angle, offset);
		
		Point2d endRotated = rotateClockwiseWithOffset(endUnrotated, angle, offset);
		
		return BoundingBoxFromPoints.forTwoPoints(
			convert3D( startRotated ),				
			convert3D( endRotated )
		);
	}
	
	private static Point3d convert3D( Point2d pnt ) {
		return new Point3d(pnt.getX(), pnt.getY(), 0);
	}
	
	/**
	 * Performs a clockwise rotation of the points about the origin, and then adds an offset
	 * 
	 * @param pnt points centered around the origin
	 * @param angle angle in radians to rotate by
	 * @param offsetToAdd point to add to the result of the rotation
	 * @return the rotated point with an offset
	 */
	private static Point2d rotateClockwiseWithOffset( Point2f pnt, float angle, Point2i offsetToAdd ) {
		Point2d rotated = rotateClockwise(pnt, angle);
		rotated.add(offsetToAdd);
		return rotated;
	}
		
	/**
	 * Rotates points clockwise around the origin
	 * 
	 * @param pnt points centered-about the origin
	 * @param angle angle in radians to rotate by
	 * @return the rotated points
	 */
	private static Point2d rotateClockwise( Point2f pnt, float angle ) {
		
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		
		double xRot = (sin * pnt.getY()) + (cos * pnt.getX());
		double yRot = (cos * pnt.getY()) - (sin * pnt.getX());
		
		return new Point2d(xRot, yRot);
	}
}
