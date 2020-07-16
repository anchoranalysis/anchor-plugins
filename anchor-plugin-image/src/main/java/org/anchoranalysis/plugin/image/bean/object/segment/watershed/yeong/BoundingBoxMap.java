/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.points.PointRange;
import org.anchoranalysis.image.voxel.box.VoxelBox;

final class BoundingBoxMap {

    private List<PointRange> list = new ArrayList<>();

    private HashMap<Integer, Integer> map = new HashMap<>();

    public int indexForValue(int val) {
        return map.computeIfAbsent(
                val,
                key -> {
                    int idNew = list.size();
                    list.add(null);
                    return idNew;
                });
    }

    public ObjectCollection deriveObjects(VoxelBox<IntBuffer> matS)
            throws OperationFailedException {
        return ObjectCollectionFactory.filterAndMapWithIndexFrom(
                list,
                Objects::nonNull,
                (pointRange, index) -> matS.equalMask(pointRange.deriveBoundingBox(), index + 1));
    }

    public int addPointForValue(Point3i point, int val) {
        int reorderedIndex = indexForValue(val);

        // Add the point to the bounding-box
        addPointToBox(reorderedIndex, point);
        return reorderedIndex;
    }

    /**
     * Get bounding box for a particular index, creating if not already there, and then add a point
     * to the box.
     */
    private void addPointToBox(int indx, Point3i point) {
        PointRange pointRange = list.get(indx);
        if (pointRange == null) {
            pointRange = new PointRange();
            list.set(indx, pointRange);
        }
        pointRange.add(point);
    }
}
