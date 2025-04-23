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
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.spatial.point.Point3d;

/** Helper class for fitting ellipsoids to point data. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EllipsoidFitHelper {

    /**
     * Creates a {@link FitResult} from the matrix A and center matrix.
     *
     * @param matrixA the matrix A representing the ellipsoid
     * @param matrixCenter the matrix representing the center of the ellipsoid
     * @param suppressZCovariance whether to suppress Z covariance
     * @return a {@link FitResult} containing the fitted ellipsoid parameters
     * @throws PointsFitterException if there's an error during the fitting process
     */
    public static FitResult createFitResultFromMatrixAandCenter(
            DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter, boolean suppressZCovariance)
            throws PointsFitterException {

        DoubleMatrix2D matrixR = createMatrixR(matrixA, matrixCenter);

        FitResult fitResult = new FitResult();

        setRadiiFromDecomposition(fitResult, createMatrixE(matrixR, suppressZCovariance));
        setCenterFromFirstColumn(fitResult, matrixCenter);

        assert (!Double.isNaN(fitResult.getRadiusX()));
        assert (!Double.isNaN(fitResult.getRadiusY()));
        assert (!Double.isNaN(fitResult.getRadiusZ()));
        assert (fitResult.getRadiusX() > 0);
        assert (fitResult.getRadiusY() > 0);
        assert (fitResult.getRadiusZ() > 0);

        return fitResult;
    }

    /**
     * Sets the center point of the {@link FitResult} from the first column of the center matrix.
     *
     * @param fitResult the {@link FitResult} to update
     * @param matrixCenter the matrix containing the center coordinates
     */
    private static void setCenterFromFirstColumn(FitResult fitResult, DoubleMatrix2D matrixCenter) {
        fitResult.setCenterPoint(
                new Point3d(
                        matrixCenter.get(0, 0), matrixCenter.get(1, 0), matrixCenter.get(2, 0)));
    }

    /**
     * Creates matrix E from matrix R, optionally suppressing Z covariance.
     *
     * @param matrixR the input matrix R
     * @param suppressZCovariance whether to suppress Z covariance
     * @return the created matrix E
     */
    private static DoubleMatrix2D createMatrixE(
            DoubleMatrix2D matrixR, boolean suppressZCovariance) {
        DoubleMatrix2D e = matrixR.viewPart(0, 0, 3, 3);

        double divVal = matrixR.get(3, 3) * -1;

        e.assign(cern.jet.math.Functions.div(divVal));

        if (suppressZCovariance) {
            e.set(2, 0, 0);
            e.set(2, 1, 0);
            e.set(0, 2, 0);
            e.set(1, 2, 0);
        }
        return e;
    }

    /**
     * Sets the radii and rotation matrix of the {@link FitResult} from the eigenvalue decomposition
     * of matrix E.
     *
     * @param fitResult the {@link FitResult} to update
     * @param e the matrix E to decompose
     * @throws PointsFitterException if there's an error during the eigenvalue decomposition
     */
    private static void setRadiiFromDecomposition(FitResult fitResult, DoubleMatrix2D e)
            throws PointsFitterException {
        try {
            EigenvalueDecomposition evd = new EigenvalueDecomposition(e);

            fitResult.setRadiusX(radiusFromDiagonal(evd, 0));
            fitResult.setRadiusY(radiusFromDiagonal(evd, 1));
            fitResult.setRadiusZ(radiusFromDiagonal(evd, 2));

            fitResult.setRotMatrix(evd.getV());

        } catch (ArrayIndexOutOfBoundsException e1) {
            throw new PointsFitterException(
                    String.format(
                            "Cannot init EigenValueDecomposition with arg='%s'", e.toString()));
        }
    }

    /**
     * Calculates the radius from the diagonal of the eigenvalue decomposition matrix.
     *
     * @param evd the {@link EigenvalueDecomposition}
     * @param index the index of the diagonal element to use
     * @return the calculated radius
     */
    private static double radiusFromDiagonal(EigenvalueDecomposition evd, int index) {
        double valFromDiagonal = evd.getD().get(index, index);
        return Math.sqrt(1 / Math.abs(valFromDiagonal));
    }

    /**
     * Creates matrix R from matrix A and the center matrix.
     *
     * @param matrixA the matrix A
     * @param matrixCenter the center matrix
     * @return the created matrix R
     */
    private static DoubleMatrix2D createMatrixR(
            DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter) {

        DoubleMatrix2D matrixT = DoubleFactory2D.dense.identity(4);

        matrixT.set(3, 0, matrixCenter.get(0, 0));
        matrixT.set(3, 1, matrixCenter.get(1, 0));
        matrixT.set(3, 2, matrixCenter.get(2, 0));

        return matrixT.zMult(matrixA, null).zMult(matrixT.viewDice(), null);
    }
}
