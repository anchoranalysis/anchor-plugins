/* (C)2020 */
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
                        intersection.relPosTo(objectWrite.getBoundingBox()), intersection.extent());

        BoundingBox bboxRelRead =
                new BoundingBox(
                        intersection.relPosTo(objectRead.getBoundingBox()), intersection.extent());

        // TODO we can make this more efficient, as we only need to duplicate the intersection area
        ObjectMask objectReadDuplicated = objectRead.duplicate();
        ObjectMask objectWriteDuplicated = objectWrite.duplicate();

        objectWrite
                .getVoxelBox()
                .setPixelsCheckMask(
                        bboxRelWrite,
                        objectReadDuplicated.getVoxelBox(),
                        bboxRelRead,
                        objectWrite.getBinaryValues().getOffInt(),
                        objectReadDuplicated.getBinaryValuesByte().getOnByte());

        objectRead
                .getVoxelBox()
                .setPixelsCheckMask(
                        bboxRelRead,
                        objectWriteDuplicated.getVoxelBox(),
                        bboxRelWrite,
                        objectRead.getBinaryValues().getOffInt(),
                        objectWriteDuplicated.getBinaryValuesByte().getOnByte());
    }

    private void removeIntersectingVoxelsIfIntersects(
            ObjectMask objectWrite, ObjectMask objectRead, ImageDimensions dimensions) {
        Optional<BoundingBox> intersection =
                objectWrite
                        .getBoundingBox()
                        .intersection()
                        .withInside(objectRead.getBoundingBox(), dimensions.getExtent());

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
