/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Reflects points in each axes if all points are within a certain distance from it
 *
 * @author Owen Feehan
 */
public class ReflectInAxesWithinDistance extends PointsFitter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitter pointsFitter;

    @BeanField @NonNegative @Getter @Setter
    private double distanceX = -1; // Forces user to set a default

    @BeanField @NonNegative @Getter @Setter
    private double distanceY = -1; // Forces user to set a default

    @BeanField @NonNegative @Getter @Setter
    private double distanceZ = -1; // Forces user to set a default
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return pointsFitter.isCompatibleWith(testMark);
    }

    @Override
    public void fit(List<Point3f> points, Mark mark, ImageDimensions dimensions)
            throws PointsFitterException, InsufficientPointsException {

        if (points.isEmpty()) {
            pointsFitter.fit(points, mark, dimensions);
        }

        double[] arrDistances = new double[] {distanceX, distanceY, distanceZ};

        List<Point3f> pointsCurrent = points;
        Extent extent = dimensions.getExtent();

        // Try each dimension: x, y, and z respectively
        for (int d = 0; d < 3; d++) {

            // We try the min side and the max side
            for (int side = 0; side < 1; side++) {

                // Are all points within
                if (arePointsWithinDistanceOfBorder(points, extent, d, side == 0, arrDistances)) {
                    pointsCurrent = reflectInDimension(pointsCurrent, extent, d, side == 0);

                    // It's not allowed be on both sides of the same dimensions
                    break;
                }
            }
        }

        pointsFitter.fit(pointsCurrent, mark, dimensions);
    }

    private static List<Point3f> reflectInDimension(
            List<Point3f> pointsIn, Extent extent, int dimension, boolean min) {
        return FunctionalList.flatMapToList(
                pointsIn,
                point ->
                        Stream.of( // Both the existing point and the reflected point
                                point, reflectPoint(point, extent, dimension, min)));
    }

    private static Point3f reflectPoint(Point3f point, Extent extent, int dimension, boolean min) {
        Point3f pointReflected = new Point3f(point);
        pointReflected.setValueByDimension(
                dimension, reflectedValue(point, extent, dimension, min));
        return pointReflected;
    }

    private static float reflectedValue(Point3f point, Extent extent, int dimension, boolean min) {
        float unreflectedValue = point.getValueByDimension(dimension);
        if (min) {
            return unreflectedValue * -1;
        } else {
            float extentInDimension = extent.getValueByDimension(dimension);
            return (2 * extentInDimension) - unreflectedValue;
        }
    }

    // Min=true means the lower side,  Min=false means the higher side of the dimension
    private static boolean arePointsWithinDistanceOfBorder(
            List<Point3f> points,
            Extent extent,
            int dimension,
            boolean min,
            double[] arrDistances) {

        double dimensionMax = extent.getValueByDimension(dimension);
        double maxAllowedDistance = arrDistances[dimension];

        for (Point3f point : points) {

            double valueForDimension = point.getValueByDimension(dimension);
            double distance = min ? valueForDimension : dimensionMax - valueForDimension;

            if (distance > maxAllowedDistance) {
                return false;
            }
        }
        return true;
    }
}
