/* (C)2020 */
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
