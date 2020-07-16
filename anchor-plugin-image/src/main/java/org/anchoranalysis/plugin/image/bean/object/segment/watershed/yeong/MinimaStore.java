/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

// Stores minima points
class MinimaStore {

    private List<Minima> list = new ArrayList<>();

    /**
     * Adds a new minima, but duplicates the point (which is mutable and may change during
     * iteration) before adding
     */
    public void addDuplicated(Point3i point) {
        list.add(new Minima(new Point3i(point)));
    }

    public void add(List<Point3i> points) {
        list.add(new Minima(points));
    }

    public ObjectCollection createObjects() throws CreateException {
        return ObjectCollectionFactory.mapFrom(
                list, minima -> CreateObjectFromPoints.create(minima.getListPoints()));
    }

    public int size() {
        return list.size();
    }
}
