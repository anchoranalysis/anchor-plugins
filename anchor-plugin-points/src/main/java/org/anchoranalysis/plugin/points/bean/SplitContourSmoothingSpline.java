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
import org.anchoranalysis.spatial.point.Contour;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.Point3i;
import umontreal.ssj.functionfit.SmoothingCubicSpline;

/** Applies smoothing splines to contours and splits them at critical points. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SplitContourSmoothingSpline {

    /**
     * Applies smoothing splines to contours and splits them at critical points.
     *
     * <p>1. Fits a smoothed-spline to the contour
     *
     * <p>2. Splits this at each optima (critical points i.e. zero-crossing of first-derivative)
     *
     * @param object object-mask representing a closed contour
     * @param rho smoothing factor [0,1] as per SSJ documentation
     * @param numberLoopPoints number of points to loop back to the start of the contour
     * @param minimumNumberPoints if less than this number of points, object is returned unchanged
     * @return a {@link List} of {@link Contour}s after smoothing and splitting
     * @throws OperationFailedException if the operation fails
     */
    public static List<Contour> apply(
            ObjectMask object, double rho, int numberLoopPoints, int minimumNumberPoints)
            throws OperationFailedException {

        return traversePointsAndCallFitter(
                object,
                minimumNumberPoints,
                (pts, out) -> fitSplinesAndExtract(pts, rho, pts, numberLoopPoints, out));
    }

    /**
     * Traverses points in the object and calls the fitter function.
     *
     * @param object the {@link ObjectMask} to traverse
     * @param minimumNumberPoints minimum number of points required
     * @param fitter the {@link FitSplinesExtract} function to apply
     * @return a {@link List} of {@link Contour}s after fitting
     * @throws OperationFailedException if the operation fails
     */
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

    /**
     * Adds splines for a contour if it meets the minimum number of points requirement.
     *
     * @param contourIn the input {@link Contour}
     * @param contoursOut the output {@link List} of {@link Contour}s
     * @param fitter the {@link FitSplinesExtract} function to apply
     * @param minNumPoints minimum number of points required
     */
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

    /** Functional interface for fitting splines and extracting contours. */
    @FunctionalInterface
    private static interface FitSplinesExtract {
        /**
         * Applies the spline fitting and extraction.
         *
         * @param pointsTraversed the input points
         * @param out the output {@link List} of {@link Contour}s
         */
        public void apply(List<Point3i> pointsTraversed, List<Contour> out);
    }

    /**
     * Fits splines to the points and extracts contours.
     *
     * @param pointsToFit the {@link List} of {@link Point3i} to fit splines to
     * @param rho the smoothing factor for the spline
     * @param pointsExtra additional points to include in the fit
     * @param numExtraPoints number of extra points to include
     * @param out the {@link List} of {@link Contour}s to add the extracted contours to
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

    /**
     * Extracts and splits contours from the splines.
     *
     * @param splineX the {@link SmoothingCubicSpline} for X coordinates
     * @param splineY the {@link SmoothingCubicSpline} for Y coordinates
     * @param maxEvalPoint the maximum evaluation point
     * @param out the {@link List} of {@link Contour}s to add the extracted contours to
     * @return the updated {@link List} of {@link Contour}s
     */
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
     * Extracts coordinate values from points.
     *
     * @param points the {@link List} of {@link Point3i} to extract from
     * @param extracter the {@link ToIntFunction} to extract the coordinate
     * @param pointsExtra additional points to extract from
     * @param numberExtraPoints number of extra points to include
     * @return an array of extracted coordinate values
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

    /**
     * Creates an integer sequence as an array of doubles.
     *
     * @param size the size of the sequence
     * @return an array of doubles representing the integer sequence
     */
    private static double[] integerSequence(int size) {
        double[] out = new double[size];

        for (int i = 0; i < size; i++) {
            out[i] = i;
        }

        return out;
    }
}
