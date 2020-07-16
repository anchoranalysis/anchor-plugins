/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateObjectFromPoints {

    // TODO Optimize by requiring sorted list of points and moving through the z-stacks sequentially
    public static ObjectMask create(List<Point3i> points) throws CreateException {

        try {
            ObjectMask object = new ObjectMask(BoundingBoxFromPoints.forList(points));

            for (int i = 0; i < points.size(); i++) {

                object.binaryVoxelBox()
                        .setOn(
                                Point3i.immutableSubtract(
                                        points.get(i), object.getBoundingBox().cornerMin()));
            }

            return object;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
