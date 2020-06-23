package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import java.util.function.BiFunction;

import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.feature.calc.FeatureCalcException;

class OverlapRatioUtilities {
	
	private OverlapRatioUtilities() {}

	/** Returns {@link Math::max} or {@link Math::min} depending on a flag */
	public static BiFunction<Long,Long,Long> maxOrMin( boolean useMax ) {
		return useMax ? Math::max : Math::min;
	}
	
	public static double calcOverlapRatio( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap, int regionID, boolean mip, BiFunction<Long,Long,Long> funcAgg ) throws FeatureCalcException {
		
		if (overlap==0.0) {
			return 0.0;
		}
		
		if (mip) {
			return overlap;
		} else {
			double volume = calcVolumeAgg(
				obj1,
				obj2,
				regionID,
				funcAgg
			);
			return overlap / volume;
		}
	}
	
	private static double calcVolumeAgg(PxlMarkMemo obj1, PxlMarkMemo obj2, int regionID, BiFunction<Long,Long,Long> funcAgg) throws FeatureCalcException {
		long size1 = sizeFromMemo(obj1, regionID);
		long size2 = sizeFromMemo(obj2, regionID);
		return funcAgg.apply(size1, size2);
	}
	
	private static long sizeFromMemo( PxlMarkMemo obj, int regionID ) {
		return obj.doOperation().statisticsForAllSlices(0, regionID).size();
	}
}
