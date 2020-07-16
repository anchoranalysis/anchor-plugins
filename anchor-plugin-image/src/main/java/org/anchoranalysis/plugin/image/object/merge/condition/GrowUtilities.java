/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class GrowUtilities {

    private static Point3i subExtent = new Point3i(1, 1, 1);

    public static BoundingBox grow(BoundingBox boundingBox) {

        Point3i cornerMin = new Point3i(boundingBox.cornerMin());
        cornerMin.subtract(subExtent);
        return new BoundingBox(cornerMin, boundingBox.extent().growBy(2));
    }
}
