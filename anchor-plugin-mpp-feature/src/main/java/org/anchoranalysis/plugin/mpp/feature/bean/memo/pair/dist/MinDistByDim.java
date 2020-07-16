package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.dist;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.SessionInput;

/**
 * The minimum distance in any one particular axis-aligned direction (i.e. taking the distance as a vector, the minimum element in the vector)
 * 
 * @author Owen Feehan
 *
 */
public class MinDistByDim extends FeaturePairMemo {

	@Override
	public double calc( SessionInput<FeatureInputPairMemo> input ) {
		
		FeatureInputPairMemo params = input.get();
			
		Point3d cp = distanceVector(
			params.getObj1().getMark().centerPoint(),
			params.getObj2().getMark().centerPoint()
		);
		
		return minDimension(
			cp,
			params.getObj1().getMark().numDims() >= 3
		);
	}
	
	/** Calculates the distance between two points in each dimension independently */
	private static Point3d distanceVector(Point3d point1, Point3d point2) {
		Point3d cp = new Point3d(point1);
		cp.subtract(point2);
		cp.absolute();
		return cp;
	}
	
	private static double minDimension(Point3d cp, boolean hasZ) {
		double min = Math.min( cp.getX(), cp.getY() );
		if (hasZ) {
			min = Math.min(min, cp.getZ() );
		}
		return min;
	}
}
