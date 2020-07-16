/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistanceAlongContour extends VisitScheduler {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScalarProposer maxDistanceProposer;
    // END BEAN PROPERTIES

    private double maxDistance;

    @Override
    public void beforeCreateObject(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws InitException {
        try {
            maxDistance = maxDistanceProposer.propose(randomNumberGenerator, res);

            assert (maxDistance > 0);
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }

    @Override
    public Optional<Tuple3i> maxDistanceFromRootPoint(ImageResolution res) {
        int maxDistanceInt = (int) Math.ceil(this.maxDistance);
        assert (maxDistanceInt > 0);
        return Optional.of(new Point3i(maxDistanceInt, maxDistanceInt, maxDistanceInt));
    }

    @Override
    public void afterCreateObject(
            Point3i root, ImageResolution res, RandomNumberGenerator randomNumberGenerator)
            throws InitException {
        // NOTHING TO DO
    }

    @Override
    public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
        return (distanceAlongContour <= maxDistance);
    }
}
