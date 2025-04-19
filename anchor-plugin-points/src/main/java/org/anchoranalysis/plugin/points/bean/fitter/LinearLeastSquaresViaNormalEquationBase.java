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

package org.anchoranalysis.plugin.points.bean.fitter;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.List;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.spatial.point.Point3f;

/** Base class for linear least squares fitting using the normal equation method. */
public abstract class LinearLeastSquaresViaNormalEquationBase extends ConicFitterBase {

    @Override
    public void fit(List<Point3f> points, Mark mark, Dimensions dimensions)
            throws PointsFitterException {

        if (points.size() < minNumPoints()) {
            throw new PointsFitterException(
                    String.format("Must have at least %d points to fit", minNumPoints()));
        }

        DoubleMatrix2D coefficients = solveNormalEquation(createDesignMatrix(points));
        applyCoefficientsToMark(coefficients, mark, dimensions);
    }

    /**
     * Returns the minimum number of points required for fitting.
     *
     * @return the minimum number of points
     */
    protected abstract int minNumPoints();

    /**
     * Applies the calculated coefficients to the mark.
     *
     * @param matrixV the matrix of coefficients
     * @param mark the {@link Mark} to update
     * @param dimensions the {@link Dimensions} of the image
     * @throws PointsFitterException if there's an error applying the coefficients
     */
    protected abstract void applyCoefficientsToMark(
            DoubleMatrix2D matrixV, Mark mark, Dimensions dimensions) throws PointsFitterException;

    /**
     * Creates the design matrix from the input points.
     *
     * @param points the list of {@link Point3f} to use for creating the design matrix
     * @return the design matrix as a {@link DoubleMatrix2D}
     */
    protected abstract DoubleMatrix2D createDesignMatrix(List<Point3f> points);

    /**
     * Solves the normal-equation for a Linear Least Squares system.
     *
     * <p>All our dependent variables are treated as 1 (solution to the ellipsoid matrix equation).
     *
     * @param matrixD the design matrix
     * @return a matrix of optimal coefficients
     * @throws PointsFitterException if there's an error solving the normal equation
     */
    private static DoubleMatrix2D solveNormalEquation(DoubleMatrix2D matrixD)
            throws PointsFitterException {

        DoubleMatrix2D ones = DoubleFactory2D.dense.make(matrixD.rows(), 1, 1);

        return ConicFitterUtilities.matrixLeftDivide(
                matrixD.viewDice().zMult(matrixD, null), matrixD.viewDice().zMult(ones, null));
    }
}
