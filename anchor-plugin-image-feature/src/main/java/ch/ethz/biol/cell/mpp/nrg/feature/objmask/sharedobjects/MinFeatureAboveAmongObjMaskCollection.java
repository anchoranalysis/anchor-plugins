package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

/**
 * 1. Finds all objs from an ObjMaskCollection whose bounding-boxes intersect with a particular obj.
 * 2. Calculates a pairwise-feature
 * 3. Returns the maximum 
 * 
 * @author Owen Feehan
 *
 */
public class MinFeatureAboveAmongObjMaskCollection extends FeatureAmongObjMaskCollectionSingleElem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * Only considers values greater or equal to the threshold
	 */
	@BeanField
	private double threshold = 0.0;
	// END BEAN PROPERTIES
	
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		
		if (getSearchObjs().size()==0) {
			return getValueNoObjects();
		}
		
		double minVal = Double.POSITIVE_INFINITY;
		
		ObjMask om = params.getParams().getObjMask();
		
		ObjMaskCollection intersecting = bboxRTree().intersectsWith( om );
		
		if (intersecting.size()==0) {
			return getValueNoObjects();
		}
		
		FeatureObjMaskPairParams paramsPairs = new FeatureObjMaskPairParams();
		paramsPairs.setObjMask1( om );
		paramsPairs.setNrgStack( params.getParams().getNrgStack() );
		
		// We loop through each intersecting bounding box, and take the one with the highest feature-value
		for( ObjMask omIntersects : intersecting) {
			paramsPairs.setObjMask2(omIntersects);
			
			double val = getSessionObjs().calc(paramsPairs);
			
			if (val>=threshold && val<minVal) {
				minVal = val;
			}
		}
		
		if (minVal==Double.POSITIVE_INFINITY) {
			return getValueNoObjects();
		}
		
		return minVal;
	}



}
