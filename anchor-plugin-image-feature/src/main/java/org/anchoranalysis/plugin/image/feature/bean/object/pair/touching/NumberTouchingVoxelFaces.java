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
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * A scheme for counting the touching voxels by intersection of object-masks
 *
 * <p>Specifically, one of the object-masks is dilated, and count the number of intersecting pixels
 * with another object.
 *
 * <p>However, intersection(a*,b)!=intersection(a,b*) where * is the dilation operator. Different
 * counts occur as a single-voxel can have multiple edges with the neighbor.
 *
 * <p>So it's better if we can iterate with a kernel over the edge pixels, and count the number of
 * neighbors
 *
 * <p>We do this only where the bounding-boxes (dilated by 1 pixels) intersection. So as not to
 * waste computation-time in useless areas.
 *
 * @author Owen Feehan
 */
public class NumberTouchingVoxelFaces extends TouchingVoxels {

    @Override
    protected double calculateWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox boxIntersect)
            throws FeatureCalculationException {

        ObjectMask object2Relative = RelativeUtilities.createRelMask(object2, object1);

        try {
            return ApplyKernel.applyForCount(
                    createCountKernelMask(object1, object2Relative),
                    object1.voxels(),
                    RelativeUtilities.createRelBBox(boxIntersect, object1));
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
