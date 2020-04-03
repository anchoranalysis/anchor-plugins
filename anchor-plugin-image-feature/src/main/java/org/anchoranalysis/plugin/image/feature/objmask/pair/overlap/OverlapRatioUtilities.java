package org.anchoranalysis.plugin.image.feature.objmask.pair.overlap;

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
