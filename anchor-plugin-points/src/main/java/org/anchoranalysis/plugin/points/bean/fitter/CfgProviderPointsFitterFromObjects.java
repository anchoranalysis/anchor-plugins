/* (C)2020 */
package org.anchoranalysis.plugin.points.bean.fitter;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;

public class CfgProviderPointsFitterFromObjects extends CfgProvider {

    /// START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitterToMark pointsFitter;

    @BeanField @Getter @Setter private MarkFactory markFactory;

    /** If true, Reduces the set of points by applying a convex-hull operation */
    @BeanField @Getter @Setter private boolean convexHull = true;

    /**
     * If true, if too few points exist to make a mark, or otherwise a fitting errors, it is simply
     * not included (with only a log error) If false, an exception is thrown
     */
    @BeanField @Getter @Setter private boolean ignoreFittingFailure = true;
    // END BEAN PROPERTIES

    @Override
    public Cfg create() throws CreateException {

        ImageDimensions dimensions = pointsFitter.createDim();

        return new Cfg(
                pointsFitter.createObjects().stream()
                        .mapToListOptional(object -> createMarkFromObject(object, dimensions)));
    }

    private Optional<Mark> createMarkFromObject(ObjectMask object, ImageDimensions dimensions)
            throws CreateException {
        try {
            List<Point2i> points = maybeApplyConvexHull(object);
            if (points.isEmpty()) {
                return handleFittingFailure("There are 0 points to fit with.");
            }

            return fitToMark(PointConverter.convert2iTo3f(points), dimensions);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private List<Point2i> maybeApplyConvexHull(ObjectMask object) throws OperationFailedException {
        List<Point2i> points = ConvexHullUtilities.pointsOnOutline(object);
        if (convexHull) {
            return ConvexHullUtilities.convexHull2D(points, pointsFitter.getMinNumPoints());
        } else {
            return points;
        }
    }

    private Optional<Mark> fitToMark(List<Point3f> pointsToFit, ImageDimensions dimensions)
            throws CreateException {

        Mark markOut = markFactory.create();

        try {
            pointsFitter.fitPointsToMark(pointsToFit, markOut, dimensions);
            return Optional.of(markOut);
        } catch (OperationFailedException e) {
            return handleFittingFailure(e.friendlyMessage());
        }
    }

    private Optional<Mark> handleFittingFailure(String errorMsg) throws CreateException {
        if (ignoreFittingFailure) {
            getLogger()
                    .messageLogger()
                    .logFormatted("Ignoring mark due to a fitting error. %s", errorMsg);
            return Optional.empty();
        } else {
            throw new CreateException(
                    String.format(
                            "Cannot create mark from points due to fitting error.%n%s", errorMsg));
        }
    }
}
