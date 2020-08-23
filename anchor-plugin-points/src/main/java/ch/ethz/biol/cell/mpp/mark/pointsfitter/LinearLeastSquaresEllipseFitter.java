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

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import georegression.fitting.ellipse.FitEllipseAlgebraic;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.EllipseQuadratic_F64;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.Ellipse;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.orientation.Orientation2D;

/**
 * Fits a set of points to an ellipse using a linear least squares method.
 *
 * <p>Specifically, the approach of the <a
 * href="https://www.javatips.net/api/GeoRegression-master/main/src/georegression/fitting/ellipse/FitEllipseAlgebraic.java">georegression</a>
 * library is employed.
 *
 * @author Owen Feehan
 */
public class LinearLeastSquaresEllipseFitter extends ConicFitterBase {

    // START BEAN
    @BeanField @Getter @Setter private double minRadius = 0.55;
    // END BEAN

    @Override
    public void fit(List<Point3f> points, Mark mark, Dimensions dimensions)
            throws PointsFitterException, InsufficientPointsException {

        List<Point2D_F64> pointsConvert =
                FunctionalList.mapToList(points, point -> new Point2D_F64(point.x(), point.y()));

        FitEllipseAlgebraic fitter = new FitEllipseAlgebraic();
        fitter.process(pointsConvert);

        EllipseQuadratic_F64 fittedResult = fitter.getEllipse();

        // We assume if we can't fit to an ellipse, it's because there wasn't enough points
        if (!fittedResult.isEllipse()) {
            throw new InsufficientPointsException(
                    String.format(
                            "Insufficient number of points for an ellipse-fit. There were %d points.",
                            points.size()));
        }

        applyCoefficientsToMark(fittedResult, mark);
    }

    private void applyCoefficientsToMark(EllipseQuadratic_F64 fittedResult, Mark mark)
            throws PointsFitterException {

        // We create the coefficients by adding on a -1 at the end
        DoubleMatrix1D coefficients = DoubleFactory1D.dense.make(6);
        coefficients.set(0, fittedResult.a);
        coefficients.set(1, fittedResult.b * 2);
        coefficients.set(2, fittedResult.c);
        coefficients.set(3, fittedResult.d * 2);
        coefficients.set(4, fittedResult.e * 2);
        coefficients.set(5, fittedResult.f);

        try {
            // We convert the coefficients to more useful geometric properties
            EllipseStandardFormConverter converter = new EllipseStandardFormConverter(coefficients);

            // Put values onto the Mark Ellipse
            applyToMark((Ellipse) mark, converter);

        } catch (CreateException e) {
            throw new PointsFitterException(e);
        }
    }

    private void applyToMark(Ellipse markE, EllipseStandardFormConverter converter)
            throws PointsFitterException {
        assert (!Double.isNaN(converter.getMajorAxisAngle()));

        double radiusX = converter.getSemiMajorAxis() - getSubtractRadii();
        double radiusY = converter.getSemiMinorAxis() - getSubtractRadii();
        if (radiusX <= 0 || radiusY <= 0) {
            throw new PointsFitterException("fitter returned 0 width or height");
        }

        markE.setShellRad(getShellRad());
        markE.setMarksExplicit(
                new Point3d(converter.getCenterPointX(), converter.getCenterPointY(), 0),
                new Orientation2D(converter.getMajorAxisAngle()), // the reason for -1, assuming
                // clockwise/anti-clockwise incompatibility
                new Point2d(radiusX, radiusY));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof Ellipse;
    }
}
