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
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * A scheme for counting touching voxels.
 *
 * <p>A voxel in the second object is deemed touching if it has 4-connectivity with a voxel on the
 * exterior of the first-object (source)
 *
 * <p>In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersection, so
 * as to reduce computation-time.
 *
 * @author Owen Feehan
 */
public class NumberTouchingVoxels extends TouchingVoxels {

    @Override
    protected double calculateWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox boxIntersect)
            throws FeatureCalculationException {
        // As this means of measuring the touching pixels can differ slightly depending on om1->om2
        // or om2->om1, it's done in both directions.
        try {
            return Math.max(
                    numberTouchingFrom(object1, object2, boxIntersect),
                    numberTouchingFrom(object2, object1, boxIntersect));

        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private int numberTouchingFrom(
            ObjectMask source, ObjectMask destination, BoundingBox boxIntersect)
            throws OperationFailedException {
        BoundingBox boxIntersectRelative = RelativeUtilities.relativizeBox(boxIntersect, source);
        return calculateNeighborhoodTouchingPixels(source, destination, boxIntersectRelative);
    }

    private int calculateNeighborhoodTouchingPixels(
            ObjectMask source, ObjectMask destination, BoundingBox boxIntersectRelative)
            throws OperationFailedException {

        CountKernelNeighborhoodMask kernelMatch =
                new CountKernelNeighborhoodMask(
                        RelativeUtilities.relativizeObject(destination, source));
        return ApplyKernel.applyForCount(
                kernelMatch, source.binaryVoxels(), boxIntersectRelative, createParams());
    }
}
