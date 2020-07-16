/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate.ellipse;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.ConicFitterBase;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

@AllArgsConstructor
class EllipseFactory {

    private static final int MIN_NUMBER_POINTS = 10;

    private final ConicFitterBase pointsFitter;

    public MarkEllipse create(ObjectMask object, ImageDimensions dimensions, double shellRad)
            throws CreateException, InsufficientPointsException {

        pointsFitter.setShellRad(shellRad);

        Set<Point3i> points = PointsFromObject.pointsFromMaskOutlineSet(object);

        if (points.size() < MIN_NUMBER_POINTS) {
            throw new InsufficientPointsException(
                    String.format(
                            "Only %d points. There must be at least %d points.",
                            points.size(), MIN_NUMBER_POINTS));
        }

        return createEllipse(points, dimensions);
    }

    private MarkEllipse createEllipse(Set<Point3i> points, ImageDimensions dim)
            throws InsufficientPointsException, CreateException {

        MarkEllipse mark = new MarkEllipse();

        List<Point3f> pointsAsFloat =
                FunctionalList.mapToList(points, PointConverter::floatFromIntDropZ);

        try {
            pointsFitter.fit(pointsAsFloat, mark, dim);
        } catch (PointsFitterException e) {
            throw new CreateException(e);
        }

        return mark;
    }
}
