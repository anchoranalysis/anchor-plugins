package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

/*-
 * #%L
 * anchor-plugin-image-feature
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
public class MaxFeatureAmongObjMaskCollection extends FeatureAmongObjMaskCollectionSingleElem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	@Override
	public double calcCast(FeatureObjMaskParams params) throws FeatureCalcException {
		
		assert( getSearchObjs()!=null );
		if (getSearchObjs().size()==0) {
			return getValueNoObjects();
		}
		
		double maxVal = Double.NEGATIVE_INFINITY;
		
		ObjMask om = params.getObjMask();
		
		ObjMaskCollection intersecting = bboxRTree().intersectsWith( om );
		
		if (intersecting.size()==0) {
			return getValueNoObjects();
		}
		
		FeatureObjMaskPairParams paramsPairs = new FeatureObjMaskPairParams();
		paramsPairs.setObjMask1( om );
		paramsPairs.setNrgStack( params.getNrgStack() );
		
		// NOTE as we are creating a different type of Parameters, we can't use our existing cache
		//   as it will be incorrect, so we create a new session
		
		// We loop through each intersecting bounding box, and take the one with the highest feature-value
		for( ObjMask omIntersects : intersecting) {
			paramsPairs.setObjMask2(omIntersects);
			
			double val = getSessionObjs().calc(paramsPairs);
			
			if (val>maxVal) {
				maxVal = val;
			}
		}
		
		return maxVal;
	}
	
}
