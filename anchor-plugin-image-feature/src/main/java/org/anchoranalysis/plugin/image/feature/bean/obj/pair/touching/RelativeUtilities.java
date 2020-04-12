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

	public static BoundingBox createRelBBox( BoundingBox bboxIntersect, ObjMask om1 ) {
		BoundingBox bboxIntersectRel = new BoundingBox( bboxIntersect.relPosTo(om1.getBoundingBox()), bboxIntersect.extnt() );
		bboxIntersectRel.clipTo( om1.getBoundingBox().extnt() );
		return bboxIntersectRel;
	}
	
	public static ObjMask createRelMask( ObjMask om1, ObjMask om2 ) {
		ObjMask om2Rel = om2.relMaskTo(om1.getBoundingBox());
		om2Rel.getBoundingBox().getCrnrMin().scale(-1);
		return om2Rel;
	}
}
