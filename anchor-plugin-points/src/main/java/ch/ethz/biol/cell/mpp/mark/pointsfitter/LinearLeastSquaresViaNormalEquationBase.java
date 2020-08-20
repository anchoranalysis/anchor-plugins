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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.List;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.Dimensions;

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

    protected abstract int minNumPoints();

    protected abstract void applyCoefficientsToMark(
            DoubleMatrix2D matrixV, Mark mark, Dimensions dimensions)
            throws PointsFitterException;

    protected abstract DoubleMatrix2D createDesignMatrix(List<Point3f> points);

    /**
     * Solves the normal-equation for a Linear Least Squares system.
     *
     * <p>All our dependent variables are treated as 1 (solution to the ellipsoid matrix equation).
     *
     * @param matrixD
     * @return a matrix of optimal coefficients
     * @throws PointsFitterException
     */
    private static DoubleMatrix2D solveNormalEquation(DoubleMatrix2D matrixD)
            throws PointsFitterException {

        DoubleMatrix2D ones = DoubleFactory2D.dense.make(matrixD.rows(), 1, 1);

        return ConicFitterUtilities.matrixLeftDivide(
                matrixD.viewDice().zMult(matrixD, null), matrixD.viewDice().zMult(ones, null));
    }
}
