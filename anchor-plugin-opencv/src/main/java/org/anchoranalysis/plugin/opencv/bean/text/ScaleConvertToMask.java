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

import java.util.List;

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.scale.ScaleFactor;

/**
 * Scales each bounding-box and converts to an object-mask
 * 
 * @author owen
 *
 */
class ScaleConvertToMask {

	public static ObjMaskCollection scaledMasksForEachBox(
		List<WithConfidence<BoundingBox>> list,
		ScaleFactor sf,
		ImageDim sceneDim
	) {
		
		ObjMaskCollection objs = new ObjMaskCollection();
		for (WithConfidence<BoundingBox> bboxWithConfidence : list) {
			
			BoundingBox bbox = bboxWithConfidence.getObj();
			assert(bbox.extnt().getZ()>=1);
			bbox.scaleXYPosAndExtnt(sf);
			
			assert( sceneDim.contains(bbox) );
		
			objs.add(
				objWithAllPixelsOn(bbox)
			);
		}
		
		return objs;
	}
	
	private static ObjMask objWithAllPixelsOn( BoundingBox bbox ) {
		ObjMask om = new ObjMask(bbox);
		om.binaryVoxelBox().setAllPixelsToOn();
		return om;
	}
}
