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
package org.anchoranalysis.plugin.image.object;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.extent.box.BoundingBox;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectIntersectionRemover {

    public static ObjectCollection removeIntersectingVoxels(
            ObjectCollection objects, Dimensions dimensions, boolean errorDisconnectedObjects)
            throws OperationFailedException {
        ObjectCollection objectsDuplicated = objects.duplicate();

        for (int i = 0; i < objects.size(); i++) {

            ObjectMask objectWrite = objectsDuplicated.get(i);

            if (errorDisconnectedObjects) {
                maybeErrorDisconnectedObjects(objectWrite, "before");
            }

            for (int j = 0; j < objects.size(); j++) {

                ObjectMask objectRead = objectsDuplicated.get(j);

                if (i < j) {
                    removeIntersectingVoxelsIfIntersects(objectWrite, objectRead, dimensions);
                }
            }

            if (errorDisconnectedObjects) {
                maybeErrorDisconnectedObjects(objectWrite, "after");
            }
        }

        return objectsDuplicated;
    }

    private static void removeIntersectingVoxelsIfIntersects(
            ObjectMask objectWrite, ObjectMask objectRead, Dimensions dimensions) {
        Optional<BoundingBox> intersection =
                objectWrite
                        .boundingBox()
                        .intersection()
                        .withInside(objectRead.boundingBox(), dimensions.extent());

        // We check if their bounding boxes intersect
        if (intersection.isPresent()) {

            // Let's get a mask for the intersecting pixels
            removeIntersectingVoxels(objectWrite, objectRead, intersection.get());
        }
    }

    private static void removeIntersectingVoxels(
            ObjectMask objectWrite, ObjectMask objectRead, BoundingBox intersection) {

        // We duplicate before we make changes
        // TODO we can make this more efficient, as we only need to duplicate the intersection area
        ObjectMask objectWriteDuplicated = objectWrite.duplicate();

        assignOffVoxels(objectWrite, objectRead, intersection);
        assignOffVoxels(objectRead, objectWriteDuplicated, intersection);
    }

    /**
     * Sets voxels to be OFF if they match a (a certain part of a) a second-mask
     *
     * @param object the object-mask
     * @param restrictTo only consider this part of the mask (expressed in global coordinates, and
     *     whose extent must be the same as {@code boxToBeAssigned}
     */
    private static void assignOffVoxels(
            ObjectMask objectToAlter, ObjectMask object, BoundingBox restrictTo) {
        objectToAlter.assignOff().toObject(object, restrictTo);
    }

    private static void maybeErrorDisconnectedObjects(ObjectMask objectWrite, String description)
            throws OperationFailedException {
        if (!objectWrite.checkIfConnected()) {
            throw new OperationFailedException(
                    String.format(
                            "Object %s becomes disconnected %s removing intersecting-pixels%n",
                            objectWrite, description));
        }
    }
}
