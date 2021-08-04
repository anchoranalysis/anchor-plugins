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

package org.anchoranalysis.plugin.points.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.opencv.CVFindContours;
import org.anchoranalysis.spatial.Contour;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.Point3i;
import umontreal.ssj.functionfit.SmoothingCubicSpline;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SplitContourSmoothingSpline {

    /**
     * 1. Fits a smoothed-spline to the contour 2. Splits this at each optima (critical points i.e.
     * zero-crossing of first-derivative)
     *
     * @param object object-mask representing a closed contour
     * @param rho smoothing factor [0,1] as per SSJ documentation
     * @param minimumNumberPoints if less than this number of points, object is returned unchanged
     * @return
     * @throws OperationFailedException
     */
    public static List<Contour> apply(
            ObjectMask object, double rho, int numberLoopPoints, int minimumNumberPoints)
            throws OperationFailedException {

        return traversePointsAndCallFitter(
                object,
                minimumNumberPoints,
                (pts, out) -> fitSplinesAndExtract(pts, rho, pts, numberLoopPoints, out));
    }

    private static List<Contour> traversePointsAndCallFitter(
            ObjectMask object, int minimumNumberPoints, FitSplinesExtract fitter)
            throws OperationFailedException {

        List<Contour> contoursTraversed = CVFindContours.contoursForObject(object);

        List<Contour> out = new ArrayList<>();

        for (Contour contour : contoursTraversed) {
            addSplinesFor(contour, out, fitter, minimumNumberPoints);
        }

        return out;
    }

    private static void addSplinesFor(
            Contour contourIn,
            List<Contour> contoursOut,
            FitSplinesExtract fitter,
            int minNumPoints) {
        if (contourIn.getPoints().size() < minNumPoints) {
            contoursOut.add(contourIn);
        } else {
            fitter.apply(contourIn.pointsDiscrete(), contoursOut);
        }
    }

    @FunctionalInterface
    private static interface FitSplinesExtract {
        public void apply(List<Point3i> pointsTraversed, List<Contour> out);
    }

    /**
     * Fits splines to the points (and maybe some points from ptsExtra)
     *
     * <p>As it can be useful to have some overlap with another contour, or to the beginning of the
     * same contour (to handle cyclical contours) ptsExtra provides a means to append some
     * additional points to be fit against.
     *
     * @param pointsToFit the points to fit the splines to
     * @param rho a smoothing factor [0, 1] see SSJ documentation
     * @param pointsExtra additional points, of which the first numExtraPoints will be appended to
     *     ptsToFit
     * @param numExtraPoints how many additional points to include
     * @param out created contours are appended to the list
     */
    private static void fitSplinesAndExtract(
            List<Point3i> pointsToFit,
            double rho,
            List<Point3i> pointsExtra,
            int numExtraPoints,
            List<Contour> out) {
        if (numExtraPoints > pointsExtra.size()) {
            numExtraPoints = pointsExtra.size();
        }

        double[] u = integerSequence(pointsToFit.size() + numExtraPoints);
        double[] x = extractFromPoint(pointsToFit, Point3i::x, pointsExtra, numExtraPoints);
        double[] y = extractFromPoint(pointsToFit, Point3i::y, pointsExtra, numExtraPoints);

        // We use two smoothing splines with respect to artificial parameter u (just an integer
        // sequence)
        extractSplitContour(
                new SmoothingCubicSpline(u, x, rho),
                new SmoothingCubicSpline(u, y, rho),
                u.length,
                out);
    }

    /** Extracts and splits contours from the splines */
    private static List<Contour> extractSplitContour(
            SmoothingCubicSpline splineX,
            SmoothingCubicSpline splineY,
            int maxEvalPoint,
            List<Contour> out) {
        double prevDerivative = Double.NaN;

        Contour contour = new Contour();

        double step = 0.05;
        double z = 0;
        while (z <= maxEvalPoint) {

            double xEval = splineX.evaluate(z);
            double yEval = splineY.evaluate(z);

            double xDerivative = splineX.derivative(z);
            double yDerivative = splineY.derivative(z);

            double derivative = xDerivative / yDerivative;

            if (!Double.isNaN(prevDerivative)
                    && !Double.isNaN(derivative)
                    && Math.signum(derivative) != Math.signum(prevDerivative)) {
                out.add(contour);
                contour = new Contour();
            }
            prevDerivative = derivative;

            contour.getPoints().add(new Point3f((float) xEval, (float) yEval, 0.0f));

            z += step;
        }

        out.add(contour);
        return out;
    }

    /**
     * @param points
     * @param extracter
     * @param numberExtraPoints repeats the first numLoopedPoints points again at end of curve (to
     *     help deal with closed curves)
     * @return
     */
    private static double[] extractFromPoint(
            List<Point3i> points,
            ToIntFunction<Point3i> extracter,
            List<Point3i> pointsExtra,
            int numberExtraPoints) {

        double[] out = new double[points.size() + numberExtraPoints];

        for (int i = 0; i < points.size(); i++) {
            out[i] = extracter.applyAsInt(points.get(i));
        }

        for (int i = 0; i < numberExtraPoints; i++) {
            out[points.size() + i] = extracter.applyAsInt(pointsExtra.get(i));
        }

        return out;
    }

    // Integer sequence starting at 0
    private static double[] integerSequence(int size) {
        double[] out = new double[size];

        for (int i = 0; i < size; i++) {
            out[i] = i;
        }

        return out;
    }
}
