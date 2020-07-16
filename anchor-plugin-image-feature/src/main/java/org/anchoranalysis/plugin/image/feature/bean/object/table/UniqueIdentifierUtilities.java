/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Generates unique identifiers for object-masks
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class UniqueIdentifierUtilities {

    /**
     * Generates a unique identifier (unique within a particular collection) for an object based
     * upon an assumption that there are no overlapping objects in this collection.
     *
     * @param obj object to generate identifier for
     * @return a string encoded with an arbitrary point that lies on the object, or "none" if the
     *     object has no points
     */
    public static String forObject(ObjectMask obj) {
        return obj.findArbitraryOnVoxel().map(UniqueIdentifierUtilities::forPoint).orElse("none");
    }

    /**
     * Generates a unique identifier (unique within a particular collection) for a pair of objects
     * based upon an assumption that there are no overlapping objects in this collection.
     *
     * @param obj1 first object
     * @param obj2 second object
     * @return a string that combines a unique identifier for the first object and one for the
     *     second object
     */
    public static String forObjectPair(ObjectMask obj1, ObjectMask obj2) {
        StringBuilder sb = new StringBuilder();
        sb.append(forObject(obj1));
        sb.append("_and_");
        sb.append(forObject(obj2));
        return sb.toString();
    }

    private static String forPoint(Point3i point) {
        return String.format("%d_%d_%d", point.getX(), point.getY(), point.getZ());
    }
}
