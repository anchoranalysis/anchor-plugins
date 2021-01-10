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

package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.kernel.KernelApplicationParameters;
import org.anchoranalysis.image.voxel.kernel.OutsideKernelPolicy;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.object.single.border.NumberVoxelsAtBorder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ShapeRegularityCalculator {

    private static final KernelApplicationParameters OUTLINE_KERNEL_PARAMETERS =
            new KernelApplicationParameters(OutsideKernelPolicy.AS_OFF, false);

    public static double calculateShapeRegularity(ObjectMask object) {
        double area = object.numberVoxelsOn();
        int perimeter = NumberVoxelsAtBorder.numberBorderPixels(object, OUTLINE_KERNEL_PARAMETERS);
        return calculateValues(area, perimeter);
    }

    private static double calculateValues(double area, int perimeter) {

        if (perimeter == 0) {
            return 0.0;
        }

        double val = ((2 * Math.PI) * Math.sqrt(area / Math.PI)) / perimeter;
        assert (!Double.isNaN(val));
        return val;
    }
}
