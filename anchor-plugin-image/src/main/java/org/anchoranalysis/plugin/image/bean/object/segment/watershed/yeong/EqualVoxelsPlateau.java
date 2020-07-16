/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point3i;

class EqualVoxelsPlateau {

    @Getter private List<PointWithNeighbor> pointsEdge = new ArrayList<>();

    @Getter private List<Point3i> pointsInner = new ArrayList<>();

    public void addEdge(Point3i point, int neighborIndex) {
        Preconditions.checkArgument(neighborIndex >= 0);
        pointsEdge.add(new PointWithNeighbor(point, neighborIndex));
    }

    public void addInner(Point3i point) {
        // We make we duplicate, as point is coming from an iterator and is mutable
        pointsInner.add(new Point3i(point));
    }

    public boolean hasPoints() {
        return !pointsEdge.isEmpty() || !pointsInner.isEmpty();
    }

    public boolean isOnlyEdge() {
        return pointsInner.isEmpty() && !pointsEdge.isEmpty();
    }

    public boolean isOnlyInner() {
        return pointsEdge.isEmpty() && !pointsInner.isEmpty();
    }

    // TODO EFFICIENCY, rewrite as two separate lists, avoids having to make new lists
    public List<Point3i> pointsEdge() {
        return FunctionalList.mapToList(pointsEdge, PointWithNeighbor::getPoint);
    }

    public boolean hasNullItems() {

        for (Point3i point : pointsInner) {
            if (point == null) {
                return true;
            }
        }

        for (PointWithNeighbor pointWithNeighbor : pointsEdge) {
            if (pointWithNeighbor == null) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return pointsEdge.size() + pointsInner.size();
    }
}
