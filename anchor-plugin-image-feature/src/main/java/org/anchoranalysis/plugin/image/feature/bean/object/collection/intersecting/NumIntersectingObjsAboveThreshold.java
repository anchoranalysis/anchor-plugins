package org.anchoranalysis.plugin.image.feature.bean.object.collection.intersecting;

import java.util.List;

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


/**
 * 1. Finds all objs from an ObjMaskCollection whose bounding-boxes intersect with a particular obj.
 * 2. Calculates a pairwise-feature
 * 3. Returns the maximum 
 * 
 * @author Owen Feehan
 *
 */
public class NumIntersectingObjsAboveThreshold extends FeatureIntersectingObjsThreshold {

	@Override
	protected double aggregateResults(List<Double> results) {
		
		int cnt = 0;
		
		// We loop through each intersecting bounding box, and take the one with the highest feature-value
		for( double val : results ) {
			
			if (val>=getThreshold()) {
				cnt++;
			}
		}
				
		return cnt;
	}
}
