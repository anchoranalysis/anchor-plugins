package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

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

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Calculates objects/bounding boxes relative to others
 * 
 * @author owen
 *
 */
class RelativeUtilities {
	
	private RelativeUtilities() {}

	/**
	 * Changes (immutably) the coordinates of a bounding-box to make them relative to another object
	 * 
	 * @param box a bounding-box
	 * @param omRelativeBase object to make the bounding-box relative to
	 * @return a new bounding box with relative co-ordinates
	 */
	public static BoundingBox createRelBBox( BoundingBox box, ObjMask omRelativeBase ) {
		BoundingBox bboxIntersectRel = new BoundingBox(
			box.relPosTo(omRelativeBase.getBoundingBox()),
			box.extent()
		);
		bboxIntersectRel.clipTo( omRelativeBase.getBoundingBox().extent() );
		return bboxIntersectRel;
	}
	
	/**
	 * Changes the coordinates of an object to make them relative to another object
	 * 
	 * @param om the object 
	 * @param omRelativeBase the other object to use as a base to make om relative to
	 * @return a new object with new bounding-box (but with identical memory used for the mask)
	 */
	public static ObjMask createRelMask( ObjMask om, ObjMask omRelativeBase ) {
		ObjMask om2Rel = om.relMaskTo(omRelativeBase.getBoundingBox());
		om2Rel.getBoundingBox().getCrnrMin().scale(-1);
		return om2Rel;
	}
}
