package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.dist;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

/**
 * Measures the amount of bounding box overlap in the Z dimension
 * 
 * Expresses it as a fraction of the minimum Z-extent
 * 
 * This is useful for measuring how much two objects overlap in Z
 * 
 * It is only calculated if there is overlap of the bounding boxes in XYZ, else 0 is returned
 */

public class BBoxZOverlapRatio extends FeaturePairMemoSingleRegion {

	// START BEAN PROPERTIES
	@BeanField
	private boolean normalize=true;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

		FeatureInputPairMemo inputSessionless = input.get();
		
		BoundingBox bbox1 = bbox(inputSessionless, p->p.getObj1());
		BoundingBox bbox2 = bbox(inputSessionless, p->p.getObj2());
		
		// Check the bounding boxes intersect in general (including XY)
		if (!bbox1.hasIntersection(bbox2)) {
			return 0.0;
		}
		
		return calcOverlap(
			bbox1,
			bbox2,
			inputSessionless.getDimensionsRequired()
		);
	}
	
	private double calcOverlap(BoundingBox bbox1, BoundingBox bbox2, ImageDim dim) {
		
		BoundingBox bboxOverlap = bbox1.intersectCreateNew(bbox2, dim.getExtnt());
		
		int minExtntZ = Math.min(
			zFor(bbox1),
			zFor(bbox2)
		);
		
		double overlapZ = (double) zFor(bboxOverlap);
		
		if (normalize) {
			return overlapZ / minExtntZ;
		} else {
			return overlapZ;
		}		
	}
	
	private static int zFor(BoundingBox bbox) {
		return bbox.extnt().getZ();
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
}