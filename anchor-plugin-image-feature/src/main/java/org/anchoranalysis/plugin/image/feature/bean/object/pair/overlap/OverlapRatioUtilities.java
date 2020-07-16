/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import java.util.function.IntSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

/**
 * Calculates overlap-ratios or the denominator used for that ratio
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OverlapRatioUtilities {

    /** Calculates the overlap of two objects relative to the maximum-volume denominator */
    public static double overlapRatioToMaxVolume(FeatureInputPairObjects params) {
        return overlapRatioTo(params, () -> OverlapRatioUtilities.denominatorMaxVolume(params));
    }

    /** Calculates the overlap of two objects relative to a denominator expressed as a function */
    public static double overlapRatioTo(
            FeatureInputPairObjects params, IntSupplier denominatorFunc) {
        int intersectingVoxels = params.getFirst().countIntersectingVoxels(params.getSecond());

        if (intersectingVoxels == 0) {
            return 0;
        }

        return ((double) intersectingVoxels) / denominatorFunc.getAsInt();
    }

    /** A denominator that is the maximum-volume of the two objects */
    public static int denominatorMaxVolume(FeatureInputPairObjects params) {
        return Math.max(params.getFirst().numberVoxelsOn(), params.getSecond().numberVoxelsOn());
    }
}
