package org.anchoranalysis.plugin.image.bean.obj.filter.independent;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterPredicate;

/**
 * Keeps only objects that are not adjacent to the scene-border (i.e. have a bounding-box on the very edge of the image)
 * 
 * @author Owen Feehan
 *
 */
public class NotTouchingSceneBorder extends ObjectFilterPredicate {

	// START BEAN PROPERTIES
	@BeanField
	private boolean includeZ = false;
	// END BEAN PROPERTIES

	@Override
	protected boolean match(ObjectMask om, Optional<ImageDimensions> dim)
			throws OperationFailedException {
		
		if (!dim.isPresent()) {
			throw new OperationFailedException("Image-dimensions are required for this operation");
		}
		
		if (om.getBoundingBox().atBorderXY(dim.get())) {
			return false;
		}
		
		if (includeZ) {
			ReadableTuple3i crnrMin = om.getBoundingBox().cornerMin();
			if (crnrMin.getZ()==0) {
				return false;
			}

			ReadableTuple3i crnrMax = om.getBoundingBox().calcCornerMax();
			if (crnrMax.getZ()==(dim.get().getZ()-1)) {
				return false;
			}
		}
		return true;
	}

	public boolean isIncludeZ() {
		return includeZ;
	}

	public void setIncludeZ(boolean includeZ) {
		this.includeZ = includeZ;
	}

}
