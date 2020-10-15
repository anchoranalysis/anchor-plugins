/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.bean.feature.object.conic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.merge.ObjectMaskMerger;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.mpp.mark.Mark;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EllipticityCalculatorHelper {

    public static double calc(ObjectMask object, Mark mark, Dimensions dimensions) {
        return calc(object, objectMaskCompare(mark, dimensions));
    }

    private static double calc(ObjectMask object, ObjectMask objectCompare) {
        return calculateWithMerged(
                object, objectCompare, ObjectMaskMerger.merge(object, objectCompare));
    }

    private static double calculateWithMerged(
            ObjectMask object, ObjectMask objectCompare, ObjectMask merged) {
        int numPixelsCompare = objectCompare.numberVoxelsOn();
        int numUnion = merged.numberVoxelsOn();

        // Interseting pixels
        int numIntersection = object.countIntersectingVoxels(objectCompare);

        return intDiv(numPixelsCompare, numUnion - numIntersection + numPixelsCompare);
    }

    private static ObjectMask objectMaskCompare(Mark mark, Dimensions dim) {
        RegionMembershipWithFlags rm = RegionMapSingleton.instance().membershipWithFlagsForIndex(0);
        assert (rm.getRegionID() == 0);
        return mark.deriveObject(dim, rm, BinaryValuesByte.getDefault()).withoutProperties();
    }

    private static double intDiv(int num, int dem) {
        return ((double) num) / dem;
    }
}
