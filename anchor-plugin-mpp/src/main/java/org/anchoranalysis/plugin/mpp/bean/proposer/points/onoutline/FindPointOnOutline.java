/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points.onoutline;

import java.util.Optional;
import org.anchoranalysis.bean.NullParamsBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.orientation.Orientation;

/** Finds a pixel on the outline of an object in a particular direction */
public abstract class FindPointOnOutline extends NullParamsBean<FindPointOnOutline> {

    public abstract Optional<Point3i> pointOnOutline(Point3d centerPoint, Orientation orientation)
            throws OperationFailedException;
}
