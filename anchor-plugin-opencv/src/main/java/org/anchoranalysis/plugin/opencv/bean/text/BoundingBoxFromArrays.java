package org.anchoranalysis.plugin.opencv.bean.text;

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
import org.anchoranalysis.image.extent.BoundingBox;

/**
 * Extracts a bounding box from arrays with different co-oridinates
 * 
 * @author owen
 *
 */
class BoundingBoxFromArrays {

	public static BoundingBox boxFor( float[][] geometryArrs, int index, int offsetX, int offsetY) {
		return boxFor(
			geometryArrs[0][index],
			geometryArrs[1][index],
			geometryArrs[2][index],
			geometryArrs[3][index],
			geometryArrs[4][index],
			offsetX,
			offsetY
		);
	}
	
	private static BoundingBox boxFor( float p0, float p1, float p2, float p3, float angle, int offsetX, int offsetY) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		
		// Width and height of bounding box
		float height = p0 + p2;
		float width = p1 + p3;
		
		// Starting and ending coordinates
		double endX = offsetX + (cos * p1) + (sin * p2);
		double endY = offsetY - (sin * p1) + (cos * p2);
		
		return boxFor( (int) endX, (int) endY, (int) width, (int) height);
	}
		
	/** Create bounding box from the end-crnr and a width and height */
	private static BoundingBox boxFor( int endX, int endY, int width, int height) {

		// Force some width if 0
		if (width==0) {
			width=1;
		}
		
		// Force some height if 0
		if (height==0) {
			height=1;
		}
		
		int startX = endX - width - 1;
		int startY = endY - height - 1;
		
		assert( startX>=0 );
		assert( startY>=0 );
		
		return new BoundingBox(
			new Point3i(startX, startY, 0),
			new Point3i(endX, endY, 0)
		);
	}
}
