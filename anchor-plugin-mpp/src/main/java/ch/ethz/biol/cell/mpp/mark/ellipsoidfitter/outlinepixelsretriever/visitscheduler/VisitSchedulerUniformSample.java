/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public class VisitSchedulerUniformSample extends VisitScheduler {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<VisitScheduler> list = new ArrayList<>();
    // END BEAN PROPERTIES

    private VisitScheduler selected;

    @Override
    public Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res)
            throws OperationFailedException {
        return selected.maxDistanceFromRootPoint(res);
    }

    @Override
    public void beforeCreateObject(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws InitException {
        selected = randomNumberGenerator.sampleFromList(list);
        selected.beforeCreateObject(randomNumberGenerator, res);
    }

    @Override
    public void afterCreateObject(
            Point3i root, ImageResolution res, RandomNumberGenerator randomNumberGenerator)
            throws InitException {
        selected.afterCreateObject(root, res, randomNumberGenerator);
    }

    @Override
    public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
        return selected.considerVisit(point, distanceAlongContour, object);
    }
}
