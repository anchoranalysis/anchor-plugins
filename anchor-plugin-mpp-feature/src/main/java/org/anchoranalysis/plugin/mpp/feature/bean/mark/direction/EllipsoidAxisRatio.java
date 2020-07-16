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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.equation.QuadraticEquationSolver;
import org.anchoranalysis.math.equation.QuadraticEquationSolver.QuadraticRoots;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Computes the axis ratio of the ellipse formed by a plane of an orientation relative to the
// ellipsoid
//   intersectiong with the ellipsoid.  This is constant for all parallel planes.
//
// See paper:  Colin C. Ferguson "Intersections of Ellipsoids and Planes of Arbitrary Orientation
// and Position
//
public class EllipsoidAxisRatio extends FeatureMarkDirection {

    @Override
    protected double calcForEllipsoid(
            MarkEllipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d normalToPlane)
            throws FeatureCalcException {

        // Now we get
        QuadraticRoots roots =
                solveEquation(mark.createRadiiArray(), calcBeta(rotMatrix, normalToPlane));

        return calcRatio(roots);
    }

    private static Point3d calcBeta(RotationMatrix rotMatrix, Vector3d normalToPlane) {
        normalToPlane.normalize();
        return rotMatrix.calcRotatedPoint(new Point3d(normalToPlane));
    }

    private QuadraticRoots solveEquation(double[] radii, Point3d beta) throws FeatureCalcException {

        double a1 = Math.pow(radii[0], -2);
        double a2 = Math.pow(radii[1], -2);
        double a3 = Math.pow(radii[2], -2);

        double beta1 = beta.getX();
        double beta2 = beta.getY();
        double beta3 = beta.getZ();

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
            throw new FeatureCalcException(e);
        }
    }

    private static double calcRatio(QuadraticRoots roots) {
        double r1 = Math.sqrt(roots.getRoot1());
        double r2 = Math.sqrt(roots.getRoot2());

        double major = Math.max(r1, r2);
        double minor = Math.min(r1, r2);

        return major / minor;
    }
}
