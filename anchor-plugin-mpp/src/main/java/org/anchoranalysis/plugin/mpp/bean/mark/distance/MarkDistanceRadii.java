/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.distance;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkCircle;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkSphere;

public class MarkDistanceRadii extends MarkDistance {

    @Override
    public boolean isCompatibleWith(Mark testMark) {

        if (testMark instanceof MarkCircle) {
            return true;
        }

        if (testMark instanceof MarkEllipse) {
            return true;
        }

        if (testMark instanceof MarkEllipsoid) {
            return true;
        }

        return testMark instanceof MarkSphere;
    }

    @Override
    public double distance(Mark mark1, Mark mark2) throws UnsupportedMarkTypeException {

        if (mark1 instanceof MarkCircle && mark2 instanceof MarkCircle) {
            MarkCircle mark1Cast = (MarkCircle) mark1;
            MarkCircle mark2Cast = (MarkCircle) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof MarkSphere && mark2 instanceof MarkSphere) {
            MarkSphere mark1Cast = (MarkSphere) mark1;
            MarkSphere mark2Cast = (MarkSphere) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof MarkEllipse && mark2 instanceof MarkEllipse) {
            MarkEllipse mark1Cast = (MarkEllipse) mark1;
            MarkEllipse mark2Cast = (MarkEllipse) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        if (mark1 instanceof MarkEllipsoid && mark2 instanceof MarkEllipsoid) {
            MarkEllipsoid mark1Cast = (MarkEllipsoid) mark1;
            MarkEllipsoid mark2Cast = (MarkEllipsoid) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        throw new UnsupportedMarkTypeException("Marks not supported");
    }
}
