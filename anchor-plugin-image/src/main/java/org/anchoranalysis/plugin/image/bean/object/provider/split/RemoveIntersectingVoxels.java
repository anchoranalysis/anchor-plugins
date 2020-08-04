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

package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

/**
 * Considers all possible pairs of objects in a provider, and removes any intersecting pixels
 *
 * @author Owen Feehan
 */
public class RemoveIntersectingVoxels extends ObjectCollectionProviderWithDimensions {

    // START BEAN PROPERTIES
    /** If TRUE, throws an error if there is a disconnected object after the erosion */
    @BeanField @Getter @Setter private boolean errorDisconnectedObjects = false;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws CreateException {

        ObjectCollection objectsDuplicated = objectCollection.duplicate();

        ImageDimensions dims = createDimensions();

        for (int i = 0; i < objectCollection.size(); i++) {

            ObjectMask objectWrite = objectsDuplicated.get(i);

            maybeErrorDisconnectedObjects(objectWrite, "before");

            for (int j = 0; j < objectCollection.size(); j++) {

                ObjectMask objectRead = objectsDuplicated.get(j);

                if (i < j) {
                    removeIntersectingVoxelsIfIntersects(objectWrite, objectRead, dims);
                }
            }

            maybeErrorDisconnectedObjects(objectWrite, "after");
        }

        return objectsDuplicated;
    }

    private void removeIntersectingVoxels(
            ObjectMask objectWrite, ObjectMask objectRead, BoundingBox intersection) {

        BoundingBox bboxRelWrite =
                new BoundingBox(
                        intersection.relPosTo(objectWrite.boundingBox()), intersection.extent());

        BoundingBox bboxRelRead =
                new BoundingBox(
                        intersection.relPosTo(objectRead.boundingBox()), intersection.extent());

        // TODO we can make this more efficient, as we only need to duplicate the intersection area
        ObjectMask objectReadDuplicated = objectRead.duplicate();
        ObjectMask objectWriteDuplicated = objectWrite.duplicate();

        objectWrite
                .voxels()
                .setPixelsCheckMask(
                        bboxRelWrite,
                        objectReadDuplicated.voxels(),
                        bboxRelRead,
                        objectWrite.binaryValues().getOffInt(),
                        objectReadDuplicated.binaryValuesByte().getOnByte());

        objectRead
                .voxels()
                .setPixelsCheckMask(
                        bboxRelRead,
                        objectWriteDuplicated.voxels(),
                        bboxRelWrite,
                        objectRead.binaryValues().getOffInt(),
                        objectWriteDuplicated.binaryValuesByte().getOnByte());
    }

    private void removeIntersectingVoxelsIfIntersects(
            ObjectMask objectWrite, ObjectMask objectRead, ImageDimensions dimensions) {
        Optional<BoundingBox> intersection =
                objectWrite
                        .boundingBox()
                        .intersection()
                        .withInside(objectRead.boundingBox(), dimensions.extent());

        // We check if their bounding boxes intersect
        if (intersection.isPresent()) {

            // Let's get a mask for the intersecting pixels

            // TODO we can make this more efficient, we only need to duplicate intersection bit
            // We duplicate the originals before everything is changed
            removeIntersectingVoxels(objectWrite, objectRead, intersection.get());
        }
    }

    private void maybeErrorDisconnectedObjects(ObjectMask objectWrite, String description)
            throws CreateException {
        if (errorDisconnectedObjects) {
            try {
                if (!objectWrite.checkIfConnected()) {
                    throw new CreateException(
                            String.format(
                                    "Obj %s becomes disconnected %s removing intersecting-pixels%n",
                                    objectWrite, description));
                }
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }
    }
}
