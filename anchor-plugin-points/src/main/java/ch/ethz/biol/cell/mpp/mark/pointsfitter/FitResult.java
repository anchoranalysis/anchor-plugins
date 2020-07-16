/*-
 * #%L
 * anchor-plugin-points
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

package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import cern.colt.matrix.DoubleMatrix2D;
import lombok.Data;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.OrientationRotationMatrix;
import org.anchoranalysis.math.rotation.RotationMatrix;

@Data
public class FitResult {

    private Point3d centerPoint;
    private double radiusX;
    private double radiusY;
    private double radiusZ;
    private DoubleMatrix2D rotMatrix;

    public void applyFitResultToMark(MarkEllipsoid mark, ImageDimensions sceneDim, double shellRad)
            throws PointsFitterException {

        Orientation orientation = new OrientationRotationMatrix(new RotationMatrix(rotMatrix));

        mark.setShellRad(shellRad);
        mark.setMarksExplicit(centerPoint, orientation, radiiAsPoint());

        BoundingBox bbox = mark.bboxAllRegions(sceneDim);
        if (bbox.extent().getX() < 1 || bbox.extent().getY() < 1 || bbox.extent().getZ() < 1) {
            throw new PointsFitterException("Ellipsoid is outside scene");
        }
    }

    public void applyRadiiSubtractScale(double subtract, double scale) {
        radiusX = (radiusX - subtract) * scale;
        radiusY = (radiusY - subtract) * scale;
        radiusZ = (radiusZ - subtract) * scale;
    }

    public void imposeMinimumRadius(double minRadius) {
        // We set a minimum radius on eve
        radiusX = Math.max(radiusX, minRadius);
        radiusY = Math.max(radiusY, minRadius);
        radiusZ = Math.max(radiusZ, minRadius);
    }

    private Point3d radiiAsPoint() {
        return new Point3d(radiusX, radiusY, radiusZ);
    }
}
