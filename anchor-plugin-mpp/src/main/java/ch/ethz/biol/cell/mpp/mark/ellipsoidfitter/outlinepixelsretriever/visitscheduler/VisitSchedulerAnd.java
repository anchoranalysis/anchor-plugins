/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public class VisitSchedulerAnd extends VisitScheduler {

    // START BEAN PROPERTIES
    @BeanField private List<VisitScheduler> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res)
            throws OperationFailedException {

        Optional<Tuple3i> maxDistance = Optional.empty();

        for (VisitScheduler vs : list) {

            Optional<Tuple3i> distance = vs.maxDistanceFromRootPoint(res);

            // Skip if it doesn't return a max-distance
            if (!distance.isPresent()) {
                continue;
            }

            if (!maxDistance.isPresent()) {
                maxDistance = Optional.of(new Point3i(distance.get()));
            } else {
                maxDistance = Optional.of(maxDistance.get().min(distance.get()));
            }
        }

        return maxDistance;
    }

    @Override
    public void beforeCreateObject(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws InitException {

        for (VisitScheduler vs : list) {
            vs.beforeCreateObject(randomNumberGenerator, res);
        }
    }

    @Override
    public void afterCreateObject(
            Point3i root, ImageResolution res, RandomNumberGenerator randomNumberGenerator)
            throws InitException {

        for (VisitScheduler vs : list) {
            vs.afterCreateObject(root, res, randomNumberGenerator);
        }
    }

    @Override
    public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
        for (VisitScheduler vs : list) {
            if (!vs.considerVisit(point, distanceAlongContour, object)) {
                return false;
            }
        }
        return true;
    }

    public List<VisitScheduler> getList() {
        return list;
    }

    public void setList(List<VisitScheduler> list) {
        this.list = list;
    }
}
