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
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InverseHelper {

    private static final Algebra ALGEBRA = new Algebra();

    private static final double MACHINE_EPSILON = calculateMachineEpsilonFloat();

    public static DoubleMatrix2D inverseFor(DoubleMatrix2D matS22) {
        if (new Algebra().det(matS22) > 1e-9) {
            return ALGEBRA.inverse(matS22);
        } else {
            // Otherwise we calculate a pseudo-inverse and of matS_22 and assign it to matS_22_inv
            return pesudoInverse(matS22);
        }
    }

    // Calculates the pseudoInverse of a diagonal matrix
    // NOTE Changes the existing matrix inplace
    private static DoubleMatrix2D pesudoInverseDiag(DoubleMatrix2D mat) {

        double maxVal = Double.MIN_VALUE;
        for (int i = 0; i < mat.columns(); i++) {
            double val = mat.get(i, i);
            if (val > maxVal) {
                maxVal = val;
            }
        }

        double tol = MACHINE_EPSILON * Math.max(mat.columns(), mat.rows()) * maxVal;

        for (int i = 0; i < mat.columns(); i++) {
            double val = mat.get(i, i);
            if (val > tol) {
                mat.set(i, i, 1 / val);
            } else {
                mat.set(i, i, 0);
            }
        }
        return mat;
    }

    private static DoubleMatrix2D pesudoInverse(DoubleMatrix2D matrix) {
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        return svd.getV()
                .zMult(pesudoInverseDiag(svd.getS()), null)
                .zMult(svd.getU().viewDice(), null);
    }

    private static float calculateMachineEpsilonFloat() {
        float machEps = 1.0f;

        do {
            machEps /= 2.0f;
        } while ((float) (1.0 + (machEps / 2.0)) != 1.0);

        return machEps;
    }
}
