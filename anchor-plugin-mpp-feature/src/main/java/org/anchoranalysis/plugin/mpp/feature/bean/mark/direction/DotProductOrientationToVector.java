/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Considers the 3 directions of an Ellipsoid
public class DotProductOrientationToVector extends FeatureMarkDirection {

    @Override
    protected double calcForEllipsoid(
            MarkEllipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d directionVector)
            throws FeatureCalcException {

        double minDot = Double.POSITIVE_INFINITY;

        for (int d = 0; d < 3; d++) {
            Point3d vec = rotMatrix.column(d);

            double dot = Math.acos(directionVector.dot(vec));

            if (dot < minDot) {
                minDot = dot;
            }
        }

        return minDot;
    }
}
