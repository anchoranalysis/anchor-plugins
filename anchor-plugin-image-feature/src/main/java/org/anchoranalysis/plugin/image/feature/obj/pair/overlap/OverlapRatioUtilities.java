package org.anchoranalysis.plugin.image.feature.obj.pair.overlap;

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

import java.util.function.Supplier;

import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;

/**
 * Calculates overlap-ratios or the denominator used for that ratio
 * @author owen
 *
 */
public class OverlapRatioUtilities {

	/** Calculates the overlap of two objects relative to the maximum-volume denominator */
	public static double overlapRatioToMaxVolume(FeatureObjMaskPairParams params) {
		return overlapRatioTo(
			params,
			() -> OverlapRatioUtilities.denominatorMaxVolume(params)
		);
	}
	
	/** Calculates the overlap of two objects relative to a denominator expressed as a function */
	public static double overlapRatioTo(
		FeatureObjMaskPairParams params,
		Supplier<Integer> denominatorFunc
	) {
		int intersectingPixels = params.getObjMask1().countIntersectingPixels(
			params.getObjMask2()
		);
		
		if (intersectingPixels==0) {
			return 0;
		}
		
		return ((double) intersectingPixels) / denominatorFunc.get();
	}
	
	/** A denominator that is the maximum-volume of the two objects */
	public static int denominatorMaxVolume(FeatureObjMaskPairParams params) {
		return Math.max(
			params.getObjMask1().numPixels(),
			params.getObjMask2().numPixels()
		);
	}
}
