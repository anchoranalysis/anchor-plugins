/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.stream.IntStream;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Vector3d;
import org.anchoranalysis.spatial.rotation.RotationMatrix;

// Calculates the eccentricity of the ellipse by considering the two planes which are furtherest way
// from the Z unit-vector
// If it's an ellipsoid it calculates the Meridional Eccentricity i.e. the eccentricity
//    of the ellipse that cuts across the plane formed by the longest and shortest axes
public class EccentricityGuessXYPlane extends FeatureMarkEccentricity {

    @Override
    protected double eccentricityForEllipsoid(double[] radii, Orientation orientation) {

        Tuple2<Double, Double> pair = selectTwoRadii(radii, angles(orientation));

        return eccentricity(
                pair._1(), // Major
                pair._2() // Minor
                );
    }

    private static Double[] angles(Orientation orientation) {

        RotationMatrix rotMatrix = orientation.createRotationMatrix();

        Vector3d unitVectorZ = new Vector3d(0, 0, 1.0);
        Vector3d unitVectorZRev = new Vector3d(0, 0, -1.0);

        return IntStream.range(0, 3)
                .mapToObj(index -> angleForDimension(index, rotMatrix, unitVectorZ, unitVectorZRev))
                .toArray(Double[]::new);
    }

    private static double angleForDimension(
            int dimIndex, RotationMatrix rotMatrix, Vector3d unitVectorZ, Vector3d unitVectorZRev) {

        Vector3d rot = new Vector3d(rotMatrix.rotatedPoint(unitVectorInDirection(dimIndex)));

        return Math.min(
                cosAngleBetweenVectors(rot, unitVectorZ),
                cosAngleBetweenVectors(rot, unitVectorZRev));
    }

    private static Point3d unitVectorInDirection(int dimIndex) {
        Point3d zeroVector = new Point3d();
        zeroVector.setValueByDimension(dimIndex, 1);
        return zeroVector;
    }

    private static Tuple2<Double, Double> selectTwoRadii(double[] radii, Double[] angles) {

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
        return Tuple.of(major, minor);
    }

    //	angle between vectors (we don't bother with the arccos), as we are just finding the minimum
    // anyway
    private static double cosAngleBetweenVectors(Vector3d vec1, Vector3d vec2) {
        double num = vec1.dot(vec2);
        double dem = vec1.length() * vec2.length();
        return num / dem;
    }
}
