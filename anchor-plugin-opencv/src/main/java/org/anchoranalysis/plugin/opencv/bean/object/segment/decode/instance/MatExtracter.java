/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MatExtracter {

    /**
     * Extracts an array of <b>byte</b>s from a row in a matrix.
     *
     * @param mat the matrix.
     * @param rowIndex the index of the row in the matrix to extract.
     * @param arraySize the size of the array to extract.
     * @return a newly created array, representing the row extracted from the matrix.
     */
    public static byte[] extractRowByte(Mat mat, int rowIndex, int arraySize) {
        byte[] arr = new byte[arraySize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /**
     * Extracts an array of <b>int</b>s from a row in a matrix.
     *
     * @param mat the matrix.
     * @param rowIndex the index of the row in the matrix to extract.
     * @param arraySize the size of the array to extract.
     * @return a newly created array, representing the row extracted from the matrix.
     */
    public static int[] extractRowInt(Mat mat, int rowIndex, int arraySize) {
        int[] arr = new int[arraySize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /**
     * Extracts an array of <b>float</b>s from a row in a matrix.
     *
     * @param mat the matrix.
     * @param rowIndex the index of the row in the matrix to extract.
     * @param arraySize the size of the array to extract.
     * @return a newly created array, representing the row extracted from the matrix.
     */
    public static float[] extractRowFloat(Mat mat, int rowIndex, int arraySize) {
        float[] arr = new float[arraySize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /**
     * Extracts an array of <b>double</b>s from a row in a matrix.
     *
     * @param mat the matrix.
     * @param rowIndex the index of the row in the matrix to extract.
     * @param arraySize the size of the array to extract.
     * @return a newly created array, representing the row extracted from the matrix.
     */
    public static double[] extractRowDouble(Mat mat, int rowIndex, int arraySize) {
        double[] arr = new double[arraySize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }
}
