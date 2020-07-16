/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.geometry.Point3i;

class Minima {

    private List<Point3i> delegate;

    public Minima(List<Point3i> pointList) {
        delegate = pointList;
    }

    public Minima(Point3i point) {
        delegate = new ArrayList<>();
        delegate.add(point);
    }

    public List<Point3i> getListPoints() {
        return delegate;
    }
}
