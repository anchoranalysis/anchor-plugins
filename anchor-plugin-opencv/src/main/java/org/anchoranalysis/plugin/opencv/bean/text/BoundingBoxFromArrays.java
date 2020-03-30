package org.anchoranalysis.plugin.opencv.bean.text;

import org.anchoranalysis.anchor.mpp.mark.MarkRotatableBoundingBox;
import org.anchoranalysis.core.geometry.Point2f;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.PointConverter;

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
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation2D;

/**
 * Extracts a bounding box from arrays returned by the EAST deep-CNN model.
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
	 * @param bndScene the dimensions of the image to which the bounding-box belongs
	 * @return a bounding-box
	 */
	public static BoundingBox boxFor( float[][] geometryArrs, int index, Point2i offset, ImageDim bndScene) {
		
		Point2f startUnrotated = new Point2f(
			geometryArrs[3][index],  // distance to left-boundary of box (x-min)
			geometryArrs[0][index] 	 // distance to top-boundary of box (y-min)
		);
		
		// To bring to the same relative-direction as endUnrotated
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
			offset,
			bndScene
		);
	}
	
	private static BoundingBox boxFor( Point2f startUnrotated, Point2f endUnrotated, float angle, Point2i offset, ImageDim bndScene) {
		
		MarkRotatableBoundingBox mark = new MarkRotatableBoundingBox();
		mark.setPos( PointConverter.doubleFromInt(offset) );
		mark.update(
			PointConverter.doubleFromFloat(startUnrotated),
			PointConverter.doubleFromFloat(endUnrotated),
			new Orientation2D(-1 * angle)		// Multiply by -1 to make it clockwise rotation
		);
		return mark.bboxAllRegions(bndScene);
	}
}
