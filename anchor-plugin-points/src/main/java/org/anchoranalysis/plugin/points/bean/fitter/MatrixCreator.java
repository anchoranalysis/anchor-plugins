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
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.core.geometry.Point3f;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MatrixCreator {

    public static DoubleMatrix2D createMatrixA(DoubleMatrix1D v1, DoubleMatrix1D v2) {

        DoubleMatrix2D m = DoubleFactory2D.dense.make(4, 4);

        // Diagonals
        m.set(0, 0, v1.get(0)); // xx
        m.set(1, 1, v1.get(1)); // yy
        m.set(2, 2, v1.get(2)); // zz
        m.set(3, 3, v2.get(3));

        m.set(1, 0, v1.get(5)); // 2xy
        m.set(0, 1, v1.get(5)); // 2xy

        m.set(2, 0, v1.get(4)); // 2xz
        m.set(0, 2, v1.get(4)); // 2xz

        m.set(3, 0, v2.get(0)); // 2x
        m.set(0, 3, v2.get(0)); // 2x

        m.set(1, 2, v1.get(3)); // 2yz
        m.set(2, 1, v1.get(3)); // 2yz

        m.set(1, 3, v2.get(1)); // 2y
        m.set(3, 1, v2.get(1)); // 2y

        m.set(2, 3, v2.get(2)); // 2z
        m.set(3, 2, v2.get(2)); // 2z

        return m;
    }

    public static DoubleMatrix2D createConstraintMatrix() {

        DoubleMatrix2D mat = DoubleFactory2D.dense.make(10, 10);
        for (int i = 0; i < 6; i++) {
            mat.set(i, i, -1);
        }
        mat.set(0, 1, 1);
        mat.set(0, 2, 1);
        mat.set(1, 0, 1);
        mat.set(1, 2, 1);
        mat.set(2, 0, 1);
        mat.set(2, 1, 1);
        return mat;
    }

    public static DoubleMatrix2D createDesignMatrixWithOnes(
            List<Point3f> points, float inputPointShift) {

        // The columns are as follows: x^2 xy y^2 x y 1
        DoubleMatrix2D matrix = DoubleFactory2D.dense.make(points.size(), 10);

        for (int i = 0; i < points.size(); i++) {
            Point3f point = points.get(i);

            float x = point.x() + inputPointShift;
            float y = point.y() + inputPointShift;
            float z = point.z() + inputPointShift;

            matrix.set(i, 0, Math.pow(x, 2)); // xx
            matrix.set(i, 1, Math.pow(y, 2)); // yy
            matrix.set(i, 2, Math.pow(z, 2)); // zz

            matrix.set(i, 3, 2 * y * z); // 2yz
            matrix.set(i, 4, 2 * x * z); // 2xz
            matrix.set(i, 5, 2 * x * y); // 2xy

            matrix.set(i, 6, 2 * x); // 2x
            matrix.set(i, 7, 2 * y); // 2y
            matrix.set(i, 8, 2 * z); // 2z

            matrix.set(i, 9, 1); // 1
        }

        return matrix;
    }

    public static DoubleMatrix2D createMatrixCenter(DoubleMatrix2D matrixA, DoubleMatrix1D v2)
            throws PointsFitterException {

        DoubleMatrix2D first =
                matrixA.viewPart(0, 0, 3, 3).copy().assign(cern.jet.math.Functions.mult(-1));

        DoubleMatrix2D second = DoubleFactory2D.dense.make(3, 1);
        second.viewColumn(0).assign(v2.viewPart(0, 3));

        return ConicFitterUtilities.matrixLeftDivide(first, second);
    }
}
