/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.WithConfidence;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EastMarkExtracter {

    private static final Scalar MEAN_SUBTRACTION_CONSTANTS = new Scalar(123.68, 116.78, 103.94);

    private static final String OUTPUT_SCORES = "feature_fusion/Conv_7/Sigmoid";
    private static final String OUTPUT_GEOMETRY = "feature_fusion/concat_3";

    private static final ScaleFactorInt SCALE_BY_4 = new ScaleFactorInt(4, 4);

    /**
     * Extracts rotatable bounding boxes (as marks) from an image using the EAST model
     *
     * @param image an RGB image to extract boxes from
     * @param minConfidence filters boxes to have confidence >= minConfidence
     * @return a list of bounding-boxes, each with a confidence value
     * @throws Throwable
     */
    public static List<WithConfidence<Mark>> extractBoundingBoxes(
            ConcurrentModelPool<Net> modelPool, Mat image, double minConfidence) throws Throwable {
        return modelPool.excuteOrWait(model -> forwardPass(model, image, minConfidence));
    }

    private static List<WithConfidence<Mark>> forwardPass(
            Net model, Mat image, double minConfidence) throws ConcurrentModelException {
        try {
            model.setInput(
                    Dnn.blobFromImage(
                            image, 1.0, image.size(), MEAN_SUBTRACTION_CONSTANTS, true, false));

            List<Mat> output = new ArrayList<>();
            model.forward(output, Arrays.asList(OUTPUT_SCORES, OUTPUT_GEOMETRY));
            return extractFromMatrices(output.get(0), output.get(1), SCALE_BY_4, minConfidence);
        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }

    private static List<WithConfidence<Mark>> extractFromMatrices(
            Mat scores, Mat geometry, ScaleFactorInt offsetScale, double minConfidence) {
        Tuple2<Mat, Extent> pair = reshapeScores(scores);

        return extractFromMatricesReshaped(
                pair._1(), reshapeGeometry(geometry), pair._2(), offsetScale, minConfidence);
    }

    private static List<WithConfidence<Mark>> extractFromMatricesReshaped(
            Mat scores,
            Mat geometry,
            Extent extent,
            ScaleFactorInt offsetScale,
            double minConfidence) {
        List<WithConfidence<Mark>> list = new ArrayList<>();

        int rowsByCols = extent.volumeXY();

        float[] scoresData = arrayFromMat(scores, 0, rowsByCols);
        float[][] geometryArrs = splitGeometryIntoFiveArrays(geometry, rowsByCols);

        int index = 0;

        Point2i point = new Point2i(0, 0);
        for (point.setY(0); point.y() < extent.y(); point.incrementY()) {
            for (point.setX(0); point.x() < extent.x(); point.incrementX()) {

                float confidence = scoresData[index];
                if (confidence >= minConfidence) {

                    Mark mark =
                            RotatableBoundingBoxFromArrays.markFor(
                                    geometryArrs, index, offsetScale.scale(point));

                    list.add(new WithConfidence<>(mark, confidence));
                }
                index++;
            }
        }

        return list;
    }

    /**
     * Reshapes the geometry matrix from 4D to 2D
     *
     * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n
     * dimensional) it is necessary to reshape the matrix to be 2 dimensions only
     *
     * @param geometry matrix to reshape
     * @return the reshaped-matrix
     */
    private static Mat reshapeGeometry(Mat geometry) {
        return geometry.reshape(1, 5);
    }

    /**
     * Reshapes the scores matrix from 4d into 2d and derives the numCols/numRows
     *
     * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n
     * dimensional) it is necessary to reshape the matrix to be 2 dimensions only
     *
     * @param scores matrix to reshape
     * @return the reshaped-matrix and the numbers of rows and columns
     */
    private static Tuple2<Mat, Extent> reshapeScores(Mat scores) {

        Mat scoresReshaped = scores.reshape(1, 1);

        int rowsByCols = (int) scoresReshaped.size().width;

        // Assumes its a square matrix
        int numCols = (int) Math.floor(Math.sqrt(rowsByCols));
        int numRows = rowsByCols / numCols;

        return Tuple.of(scoresReshaped, new Extent(numCols, numRows));
    }

    private static float[][] splitGeometryIntoFiveArrays(Mat geometry, int rowsByCols) {

        float[][] out = new float[5][];
        for (int i = 0; i < 5; i++) {
            out[i] = arrayFromMat(geometry, i, rowsByCols);
        }
        return out;
    }

    /** Extracts an array of floats from a matrix */
    private static float[] arrayFromMat(Mat mat, int rowIndex, int arrSize) {
        float[] arr = new float[arrSize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }
}
