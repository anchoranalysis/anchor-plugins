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

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Vector3d;
import org.anchoranalysis.spatial.rotation.RotationMatrix;

// Considers the 3 directions of an Ellipsoid
public class DotProductOrientationToVector extends FeatureMarkDirection {

    @Override
    protected double calculateForEllipsoid(
            Ellipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d directionVector)
            throws FeatureCalculationException {

        double minDot = Double.POSITIVE_INFINITY;

        for (int d = 0; d < 3; d++) {
            Point3d vec = rotMatrix.column(d);

            double dot = Math.acos(directionVector.dotProduct(vec));

            if (dot < minDot) {
                minDot = dot;
            }
        }

        return minDot;
    }
}
