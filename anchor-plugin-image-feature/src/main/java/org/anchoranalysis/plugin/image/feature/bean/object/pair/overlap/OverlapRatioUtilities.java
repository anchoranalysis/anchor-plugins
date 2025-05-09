/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import java.util.function.IntSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;

/**
 * Utility class for calculating overlap-ratios or the denominator used for that ratio.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OverlapRatioUtilities {

    /**
     * Calculates the overlap ratio of two objects relative to the maximum-volume denominator.
     *
     * @param input the {@link FeatureInputPairObjects} containing the two objects
     * @return the overlap ratio
     */
    public static double overlapRatioToMaxVolume(FeatureInputPairObjects input) {
        return overlapRatioTo(input, () -> OverlapRatioUtilities.denominatorMaxVolume(input));
    }

    /**
     * Calculates the overlap ratio of two objects relative to a denominator expressed as a
     * function.
     *
     * @param input the {@link FeatureInputPairObjects} containing the two objects
     * @param denominatorFunc a function that supplies the denominator value
     * @return the overlap ratio
     */
    public static double overlapRatioTo(
            FeatureInputPairObjects input, IntSupplier denominatorFunc) {
        int intersectingVoxels = input.getFirst().countIntersectingVoxels(input.getSecond());

        if (intersectingVoxels == 0) {
            return 0;
        }

        return ((double) intersectingVoxels) / denominatorFunc.getAsInt();
    }

    /**
     * Calculates a denominator that is the maximum-volume of the two objects.
     *
     * @param input the {@link FeatureInputPairObjects} containing the two objects
     * @return the maximum volume of the two objects
     */
    public static int denominatorMaxVolume(FeatureInputPairObjects input) {
        return Math.max(input.getFirst().numberVoxelsOn(), input.getSecond().numberVoxelsOn());
    }
}
