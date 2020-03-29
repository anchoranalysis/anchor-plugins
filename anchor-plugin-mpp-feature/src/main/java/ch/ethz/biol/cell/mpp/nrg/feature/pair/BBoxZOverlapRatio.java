package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;

// Measures the amount of bounding box overlap in the Z dimension
//  Expresses it as a fraction of the minimum Z-extent
//
// This is useful for measuring how much two objects overlap in Z
//
// It is only calculated if there is overlap of the bounding boxes in XYZ, else 0 is returned
public class BBoxZOverlapRatio extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID = 0;
	
	@BeanField
	private boolean normalize=true;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(NRGElemPairCalcParams params)
			throws FeatureCalcException {

	
		ImageDim sd = params.getNrgStack().getDimensions();
		BoundingBox bbox1 = params.getObj1().getMark().bbox(sd,regionID);
		BoundingBox bbox2 = params.getObj2().getMark().bbox(sd,regionID);
		
		// Check the bounding boxes intersect in general (including XY)
		if (!bbox1.hasIntersection(bbox2)) {
			return 0.0;
		}
		
		BoundingBox bboxOverlap = bbox1.intersectCreateNew(bbox2, sd.getExtnt() );
		
		int minExtntZ = Math.min( bbox1.extnt().getZ(), bbox2.extnt().getZ() );
		
		if (normalize) {
			return ((double) bboxOverlap.extnt().getZ()) / minExtntZ;
		} else {
			return ((double) bboxOverlap.extnt().getZ());
		}
	
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

}
