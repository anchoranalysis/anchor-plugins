/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Calculates objects/bounding boxes relative to others
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class RelativeUtilities {

    /**
     * Changes (immutably) the coordinates of a bounding-box to make them relative to another object
     *
     * @param box a bounding-box
     * @param objectRelativeBase object to make the bounding-box relative to
     * @return a new bounding box with relative coordinates
     */
    public static BoundingBox createRelBBox(BoundingBox box, ObjectMask objectRelativeBase) {
        BoundingBox bboxIntersectRel =
                new BoundingBox(box.relPosTo(objectRelativeBase.getBoundingBox()), box.extent());
        return bboxIntersectRel.clipTo(objectRelativeBase.getBoundingBox().extent());
    }

    /**
     * Changes the coordinates of an object to make them relative to another object
     *
     * @param object the object
     * @param objectRelativeBase the other object to use as a base to make om relative to
     * @return a new object with new bounding-box (but with identical memory used for the mask)
     */
    public static ObjectMask createRelMask(ObjectMask object, ObjectMask objectRelativeBase) {
        return object.relMaskTo(objectRelativeBase.getBoundingBox())
                .mapBoundingBox(BoundingBox::reflectThroughOrigin);
    }
}
