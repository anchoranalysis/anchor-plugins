/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate.ellipsoid;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipsoidFitter;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EllipsoidFactory {

    /**
     * Creates a MarkEllipsoid using least-squares fitting to the points on the outline of an
     * object-mask
     *
     * @param object object-mask
     * @param dimensions the dimensions of the scene the object is contaiend in
     * @param suppressZCovariance whether to suppress the covariance in the z-dimension when doing
     *     least squares fiting
     * @param shellRad shellRad for the mark that is created
     * @return
     * @throws CreateException
     */
    public static MarkEllipsoid createMarkEllipsoidLeastSquares(
            ObjectMask object,
            ImageDimensions dimensions,
            boolean suppressZCovariance,
            double shellRad)
            throws CreateException {
        return createMarkEllipsoidLeastSquares(
                () -> PointsFromObject.pointsFromMaskOutline(object),
                dimensions,
                suppressZCovariance,
                shellRad);
    }

    public static MarkEllipsoid createMarkEllipsoidLeastSquares(
            Operation<List<Point3i>, CreateException> opPoints,
            ImageDimensions dimensions,
            boolean suppressZCovariance,
            double shellRad)
            throws CreateException {

        LinearLeastSquaresEllipsoidFitter pointsFitter = new LinearLeastSquaresEllipsoidFitter();
        pointsFitter.setShellRad(shellRad);
        pointsFitter.setSuppressZCovariance(suppressZCovariance);

        // Now get all the points on the outline
        MarkEllipsoid mark = new MarkEllipsoid();

        List<Point3f> pointsFloat =
                FunctionalList.mapToList(opPoints.doOperation(), PointConverter::floatFromInt);

        try {
            pointsFitter.fit(pointsFloat, mark, dimensions);
        } catch (PointsFitterException e) {
            throw new CreateException(e);
        }
        return mark;
    }
}
