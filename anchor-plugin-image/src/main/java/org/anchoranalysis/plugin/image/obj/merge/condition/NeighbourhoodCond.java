package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation;

/**
 * A condition placed to determien if two objects could be potential neighbours are not (i.e. potential candidates for merging)
 * 
 * @author Owen Feehan
 *
 */
public class NeighbourhoodCond implements UpdatableBeforeCondition {

	private boolean requireBBoxNeighbours;
	private boolean requireTouching;
	
	// START TEMPORARY objects, updated after every call to updateSrcObj
	// The bounding box of omSrcWithFeature grown by 1 in all directions (used for testing bbox intersection)
	private BoundingBox bboxSrcGrown;
	
	// The object-mask of omSrcWithFeature dilated by 1 in all directions (used for testing if objects touch)
	private ObjMask omGrown;
	// END TEMPORARY objects
	
			
	public NeighbourhoodCond(boolean requireBBoxNeighbours, boolean requireTouching) {
		super();
		
		this.requireBBoxNeighbours = requireBBoxNeighbours;
		this.requireTouching = requireTouching;
					
		if (requireTouching==true) {
			this.requireBBoxNeighbours = false;	// We don't need this check, if we're testing actual object intersection
		}
	}

	@Override
	public void updateSrcObj(ObjMask omSrc, Optional<ImageRes> res) throws OperationFailedException {

		bboxSrcGrown = requireBBoxNeighbours ? bboxGrown( omSrc ) : null;
		
		try {
			if (requireTouching) {
				omGrown = MorphologicalDilation.createDilatedObjMask(
					omSrc,
					Optional.empty(),
					true,
					1,
					false
				);
			} else {
				omGrown = null;
			}
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
	}

	@Override
	public boolean accept(ObjMask omDest) {
		
		// If this is set, we ignore any combinations whose bounding boxes don't touch or intersect
		if (requireBBoxNeighbours) {
			if (!bboxSrcGrown.hasIntersection(omDest.getBoundingBox())) {
				return false;
			}
		}
		
		if (requireTouching) {
			if (!omGrown.hasIntersectingPixels(omDest)) {
				return false;
			}
		}
		
		return true;
	}
			
	private static BoundingBox bboxGrown( ObjMask obj ) {
		return GrowUtilities.growBBox( obj.getBoundingBox() );
	}
}
