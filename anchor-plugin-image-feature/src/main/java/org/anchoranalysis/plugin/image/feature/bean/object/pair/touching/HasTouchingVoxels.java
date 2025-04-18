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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * A simple scheme for counting the touching voxels.
 *
 * <p>A voxel in the second object is touching if it has 4-connectivity with a voxel on the exterior
 * of the first-object (source)
 *
 * <p>In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersect. So as
 * not to waste computation-time in useless areas.
 *
 * @author Owen Feehan
 */
public class HasTouchingVoxels extends TouchingVoxels {

    @Override
    protected double calculateWithIntersection(
            ObjectMask first, ObjectMask second, BoundingBox boxIntersect)
            throws FeatureCalculationException {
        return convertToInt(
                calculateHasTouchingRelative(
                        first,
                        RelativeUtilities.relativizeObject(second, first),
                        RelativeUtilities.relativizeBox(boxIntersect, first)));
    }

    private boolean calculateHasTouchingRelative(
            ObjectMask first, ObjectMask secondRelative, BoundingBox boxIntersectRel)
            throws FeatureCalculationException {
        CountKernel kernelMatch = createCountKernelMask(secondRelative);
        try {
            return ApplyKernel.applyUntilPositive(
                    kernelMatch, first.binaryVoxels(), boxIntersectRel, createParameters());
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private static int convertToInt(boolean b) {
        return b ? 1 : 0;
    }
}
