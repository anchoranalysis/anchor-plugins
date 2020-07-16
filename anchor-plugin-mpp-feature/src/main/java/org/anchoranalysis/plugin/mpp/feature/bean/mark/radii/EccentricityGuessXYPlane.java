/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import java.util.stream.IntStream;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;
import org.apache.commons.math3.util.Pair;

// Calculates the eccentricity of the ellipse by considering the two planes which are furtherest way
// from the Z unit-vector
// If it's an ellipsoid it calculates the Meridional Eccentricity i.e. the eccentricity
//    of the ellipse that cuts across the plane formed by the longest and shortest axes
public class EccentricityGuessXYPlane extends FeatureMarkEccentricity {

    @Override
    protected double calcEccentricityForEllipsoid(double[] radii, Orientation orientation) {

        Pair<Double, Double> pair = selectTwoRadii(radii, calcAngles(orientation));

        return calcEccentricity(
                pair.getFirst(), // Major
                pair.getSecond() // Minor
                );
    }

    private static Double[] calcAngles(Orientation orientation) {

        RotationMatrix rotMatrix = orientation.createRotationMatrix();

        Vector3d unitVectorZ = new Vector3d(0, 0, 1.0);
        Vector3d unitVectorZRev = new Vector3d(0, 0, -1.0);

        return IntStream.range(0, 3)
                .mapToObj(
                        index ->
                                calcAngleForDimension(
                                        index, rotMatrix, unitVectorZ, unitVectorZRev))
                .toArray(Double[]::new);
    }

    private static double calcAngleForDimension(
            int dimIndex, RotationMatrix rotMatrix, Vector3d unitVectorZ, Vector3d unitVectorZRev) {

        Vector3d rot = new Vector3d(rotMatrix.calcRotatedPoint(unitVectorInDirection(dimIndex)));

        return Math.min(
                cosAngleBetweenVectors(rot, unitVectorZ),
                cosAngleBetweenVectors(rot, unitVectorZRev));
    }

    private static Point3d unitVectorInDirection(int dimIndex) {
        Point3d zeroVector = new Point3d();
        zeroVector.setValueByDimension(dimIndex, 1);
        return zeroVector;
    }

    private static Pair<Double, Double> selectTwoRadii(double[] radii, Double[] angles) {

        double xAngle = angles[0];
        double yAngle = angles[1];
        double zAngle = angles[2];

        double radiiOther1;
        double radiiOther2;
        // Find smallest angle, and select the other two radii for our eccentricty
        if (xAngle > yAngle) {
            if (xAngle > zAngle) {
                // x is the smallest
                radiiOther1 = radii[1];
                radiiOther2 = radii[2];
            } else {
                // z is the smallest
                radiiOther1 = radii[0];
                radiiOther2 = radii[1];
            }
        } else {
            if (yAngle > zAngle) {
                // y is the smallest
                radiiOther1 = radii[0];
                radiiOther2 = radii[2];
            } else {
                // z is the smallest
                radiiOther1 = radii[0];
                radiiOther2 = radii[1];
            }
        }

        double major = Math.max(radiiOther1, radiiOther2);
        double minor = Math.min(radiiOther1, radiiOther2);
        return new Pair<>(major, minor);
    }

    //	angle between vectors (we don't bother with the arccos), as we are just finding the minimum
    // anyway
    private static double cosAngleBetweenVectors(Vector3d vec1, Vector3d vec2) {
        double num = vec1.dot(vec2);
        double dem = vec1.length() * vec2.length();
        return num / dem;
    }
}
