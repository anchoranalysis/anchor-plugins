/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

import java.util.Optional;
import org.anchoranalysis.bean.NullParamsBean;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public abstract class VisitScheduler extends NullParamsBean<VisitScheduler> {

    public abstract Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res)
            throws OperationFailedException;

    public abstract void beforeCreateObject(
            RandomNumberGenerator randomNumberGenerator, ImageResolution res) throws InitException;

    public abstract void afterCreateObject(
            Point3i root, ImageResolution res, RandomNumberGenerator randomNumberGenerator)
            throws InitException;

    public abstract boolean considerVisit(
            Point3i point, int distanceAlongContour, ObjectMask object);
}
