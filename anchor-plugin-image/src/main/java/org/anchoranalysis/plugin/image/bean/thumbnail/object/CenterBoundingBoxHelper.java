/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.ReadableTuple3i;
import org.anchoranalysis.spatial.point.Tuple3i;

/**
 * Derives a new centered bounding-box (of a particular size) from an existing bounding-box
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CenterBoundingBoxHelper {

    /**
     * Derives a new centered bounding-box (of a particular size) from an existing bounding-box
     * WITHOUT going outside scene boundaries
     *
     * <p>The algorithm tries to maximally center the existing bounding-box in each dimension, but
     * will push it left, right, up, down etc. to avoid exceeding the scene boundaries.
     *
     * @param boxToBeCentered
     * @param targetSize
     * @param sceneExtent defines the boundaries of the scene
     * @return a bounding-box of size {@code targetSize} entirely containing {@code
     *     boxToBeCentered}, as centered as possible.
     * @throws OperationFailedException if the box to centered is larger than the target size
     */
    public static BoundingBox deriveCenteredBoxWithSize(
            BoundingBox boxToBeCentered, Extent targetSize, Extent sceneExtent)
            throws OperationFailedException {

        if (boxToBeCentered.extent().anyDimensionIsLargerThan(targetSize)) {
            throw new OperationFailedException(
                    String.format(
                            "The existing bounding-box (%s) is larger than the target-size (%s)",
                            boxToBeCentered.extent(), targetSize));
        }

        ReadableTuple3i targetSizeAsTuple = targetSize.asTuple();

        // How much does the centered box need to be pushed back ideally, to meet the targetSize,
        // and becentered?
        Tuple3i idealShiftDown = idealShiftDown(boxToBeCentered, targetSizeAsTuple);

        // Check if this goes over the bottom boundary, and push up if necessary
        Point3i cornerLeft = Point3i.immutableSubtract(boxToBeCentered.cornerMin(), idealShiftDown);
        pushUpIfNecessary(cornerLeft);

        // Check if this goes over the top boundary, and push down if necessary
        return pushDownIfNecessary(cornerLeft, targetSizeAsTuple, sceneExtent);
    }

    private static Tuple3i idealShiftDown(
            BoundingBox boxToBeCentered, ReadableTuple3i targetSizeAsTuple) {
        // How much does the centered box need to grow in each dimension, to meet the targetSize?
        Tuple3i difference =
                Point3i.immutableSubtract(targetSizeAsTuple, boxToBeCentered.extent().asTuple());

        // Subtracting half the difference
        difference.scale(0.5);

        return difference;
    }

    private static void pushUpIfNecessary(Point3i cornerLeft) {
        // If we've gone less than 0 in any dimension, then we correct by pushing the value up
        Point3i toSubtract = keepOnlyNegative(cornerLeft);
        cornerLeft.subtract(toSubtract);
    }

    private static BoundingBox pushDownIfNecessary(
            Point3i cornerLeft, ReadableTuple3i targetSizeAsTuple, Extent sceneExtent) {

        // The hypothethical right-corner
        Point3i cornerRight = Point3i.immutableAdd(cornerLeft, targetSizeAsTuple);

        // If we've gone beyond the scene extent in any dimension, then we correct by pushing the
        // value down
        Point3i toSubtract = keepOnlyExcess(cornerRight, sceneExtent);

        cornerLeft.subtract(toSubtract);
        cornerRight.subtract(toSubtract);
        cornerRight.subtract(1);

        assert (sceneExtent.contains(cornerLeft));
        assert (sceneExtent.contains(cornerRight));

        return new BoundingBox(cornerLeft, cornerRight);
    }

    /** Creates a new point where negative-values are retained, and non-negative values are zero */
    private static Point3i keepOnlyNegative(Point3i point) {
        return new Point3i(
                negativeOrZero(point.x()), negativeOrZero(point.y()), negativeOrZero(point.z()));
    }

    /**
     * Creates a new point where values at the scene-extent's boundary or beyond (the excess) are
     * kept, and any values below these boundaries are set to 0
     */
    private static Point3i keepOnlyExcess(Point3i point, Extent sceneExtent) {
        return new Point3i(
                keepOnlyExcess(point.x(), sceneExtent.x()),
                keepOnlyExcess(point.y(), sceneExtent.y()),
                keepOnlyExcess(point.z(), sceneExtent.z()));
    }

    private static int negativeOrZero(int value) {
        if (value < 0) {
            return value;
        } else {
            return 0;
        }
    }

    private static int keepOnlyExcess(int value, int boundary) {
        if (value > boundary) {
            return value - boundary;
        } else {
            return 0;
        }
    }
}
