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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.math.equation.QuadraticEquationSolver;
import org.anchoranalysis.math.equation.QuadraticEquationSolver.QuadraticRoots;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.spatial.orientation.Orientation;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Vector3d;

// Computes the axis ratio of the ellipse formed by a plane of an orientation relative to the
// ellipsoid
//   intersectiong with the ellipsoid.  This is constant for all parallel planes.
//
// See paper:  Colin C. Ferguson "Intersections of Ellipsoids and Planes of Arbitrary Orientation
// and Position
//
public class EllipsoidAxisRatio extends FeatureMarkDirection {

    @Override
    protected double calculateForEllipsoid(
            Ellipsoid mark, Orientation orientation, Vector3d normalToPlane)
            throws FeatureCalculationException {

        // Now we get
        QuadraticRoots roots =
                solveEquation(mark.createRadiiArray(), calculateBeta(orientation, normalToPlane));

        return ratio(roots);
    }

    private static Point3d calculateBeta(Orientation orientation, Vector3d normalToPlane) {
        return orientation.getRotationMatrix().rotatePoint(normalToPlane);
    }

    private QuadraticRoots solveEquation(double[] radii, Point3d beta)
            throws FeatureCalculationException {

        double a1 = Math.pow(radii[0], -2);
        double a2 = Math.pow(radii[1], -2);
        double a3 = Math.pow(radii[2], -2);

        double beta1 = beta.x();
        double beta2 = beta.y();
        double beta3 = beta.z();

        double beta1Squared = Math.pow(beta1, 2);
        double beta2Squared = Math.pow(beta2, 2);
        double beta3Squared = Math.pow(beta3, 2);

        double eqXSquared =
                (a2 * a3 * beta1Squared) + (a1 * a3 * beta2Squared) + (a1 * a2 * beta3Squared);
        double eqX =
                -1
                        * (((a2 + a3) * beta1Squared)
                                + ((a1 + a3) * beta2Squared)
                                + ((a1 + a2) * beta3Squared));
        double eq = 1;

        try {
            return QuadraticEquationSolver.solveQuadraticEquation(eqXSquared, eqX, eq);
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private static double ratio(QuadraticRoots roots) {
        double r1 = Math.sqrt(roots.getRoot1());
        double r2 = Math.sqrt(roots.getRoot2());

        double major = Math.max(r1, r2);
        double minor = Math.min(r1, r2);

        return major / minor;
    }
}
