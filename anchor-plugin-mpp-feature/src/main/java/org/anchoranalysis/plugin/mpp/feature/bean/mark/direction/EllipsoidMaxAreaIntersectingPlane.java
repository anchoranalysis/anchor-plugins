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

import org.anchoranalysis.anchor.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Computes the axis ratio of the ellipse formed by a plane of an orientation relative to the
// ellipsoid
//   intersectiong with the ellipsoid.  This is constant for all parallel planes.
//
// See paper:  Colin C. Ferguson "Intersections of Ellipsoids and Planes of Arbitrary Orientation
// and Position
//
public class EllipsoidMaxAreaIntersectingPlane extends FeatureMarkDirection {

    @Override
    protected double calculateForEllipsoid(
            Ellipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d normalToPlane)
            throws FeatureCalculationException {

        double[] radii = mark.createRadiiArray();

        normalToPlane.normalize();
        Point3d beta = rotMatrix.rotatedPoint(new Point3d(normalToPlane));

        double beta1Squared = Math.pow(beta.x(), 2);
        double beta2Squared = Math.pow(beta.y(), 2);
        double beta3Squared = Math.pow(beta.z(), 2);

        double pt =
                Math.sqrt(
                        (Math.pow(radii[0], 2) * beta1Squared)
                                + (Math.pow(radii[1], 2) * beta2Squared)
                                + (Math.pow(radii[2], 2) * beta3Squared));

        return (Math.PI * radii[0] * radii[1] * radii[2]) / pt;
    }
}
