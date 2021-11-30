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

package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.points.RotatableBoundingBox;
import org.anchoranalysis.mpp.mark.points.RotatableBoundingBoxFactory;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.scale.ScaleFactorInt;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EastMarkExtracter {

    private static final String CLASS_LABEL = "text";

    private static final ScaleFactorInt SCALE_BY_4 = new ScaleFactorInt(4, 4);

    public static List<LabelledWithConfidence<Mark>> decode(
            List<Mat> output, double minConfidence) {
        return extractFromMatrices(output.get(0), output.get(1), SCALE_BY_4, minConfidence);
    }

    private static List<LabelledWithConfidence<Mark>> extractFromMatrices(
            Mat scores, Mat geometry, ScaleFactorInt offsetScale, double minConfidence) {
        Tuple2<Mat, Extent> pair = reshapeScores(scores);

        return extractFromMatricesReshaped(
                pair._1(), reshapeGeometry(geometry), pair._2(), offsetScale, minConfidence);
    }

    private static List<LabelledWithConfidence<Mark>> extractFromMatricesReshaped(
            Mat scores,
            Mat geometry,
            Extent extent,
            ScaleFactorInt offsetScale,
            double minConfidence) {
        List<LabelledWithConfidence<Mark>> list = new ArrayList<>();

        int rowsByCols = extent.areaXY();

        float[] scoresData = MatExtracter.extractRowFloat(scores, 0, rowsByCols);
        float[][] geometryArrs = splitGeometryIntoFiveArrays(geometry, rowsByCols);

        extent.iterateOverXYOffset(
                (point, offset) -> {
                    float confidence = scoresData[offset];
                    if (confidence >= minConfidence) {

                        Mark mark = markFor(geometryArrs, offset, offsetScale.scale(point));

                        list.add(new LabelledWithConfidence<>(mark, confidence, CLASS_LABEL));
                    }
                });

        return list;
    }

    /**
     * Builds a bounding-box from the arrays returned from the EAST algorithm.
     *
     * <p>The bounding-boxes are encoded in RBOX format, see the original EAST paper Zhou et al.
     * 2017.
     *
     * <p>A C++ example of calling EAST via OpenCV can be found on <a
     * href="https://github.com/opencv/opencv/blob/master/samples/dnn/text_detection.cpp">GitHub</a>.
     *
     * @param geometryArrs an array of 5 arrays.
     * @param index the current index to look in each of the 5 arrays.
     * @param offset an offset in the scene to add to each generated bounding-box.
     * @return a mark encapsulating a rotatable bounding-box.
     */
    private static RotatableBoundingBox markFor(float[][] geometryArrs, int index, Point2i offset) {
        return RotatableBoundingBoxFactory.create(
                vectorIndex -> geometryArrs[vectorIndex][index], offset);
    }

    /**
     * Reshapes the geometry matrix from 4D to 2D.
     *
     * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n
     * dimensional), it is necessary to reshape the matrix to be 2 dimensions only.
     *
     * @param geometry matrix to reshape.
     * @return the reshaped-matrix.
     */
    private static Mat reshapeGeometry(Mat geometry) {
        return geometry.reshape(1, 5);
    }

    /**
     * Reshapes the scores matrix from 4d into 2d and derives the number of columns and rows.
     *
     * <p>As the Java interface to OpenCV only supports 2 dimensional matrices (rather than n
     * dimensional) it is necessary to reshape the matrix to be 2 dimensions only.
     *
     * @param scores matrix to reshape.
     * @return the reshaped-matrix and the numbers of rows and columns.
     */
    private static Tuple2<Mat, Extent> reshapeScores(Mat scores) {

        Mat scoresReshaped = scores.reshape(1, 1);

        int rowsByColumns = (int) scoresReshaped.size().width;

        // Assumes its a square matrix
        int numberColumns = (int) Math.floor(Math.sqrt(rowsByColumns));
        int numberRows = rowsByColumns / numberColumns;

        return Tuple.of(scoresReshaped, new Extent(numberColumns, numberRows));
    }

    private static float[][] splitGeometryIntoFiveArrays(Mat geometry, int rowsByCols) {

        float[][] out = new float[5][];
        for (int i = 0; i < 5; i++) {
            out[i] = MatExtracter.extractRowFloat(geometry, i, rowsByCols);
        }
        return out;
    }
}
