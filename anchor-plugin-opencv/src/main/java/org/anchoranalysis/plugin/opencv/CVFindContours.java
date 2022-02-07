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

package org.anchoranalysis.plugin.opencv;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.point.Contour;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.ReadableTuple3i;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * Wrapper around OpenCV's <a
 * href="https://docs.opencv.org/3.4.15/df/d0d/tutorial_find_contours.html">findContours</a>
 * function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CVFindContours {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    /**
     * Calculates the extreme outer contours from an {@link ObjectMask} as OpenCV defines <a
     * href="https://docs.opencv.org/3.4/d4/d73/tutorial_py_contours_begin.html">contours</a>.
     *
     * <p>The setting {@link Imgproc#RETR_EXTERNAL} defines the extreme outer contours. Please see
     * the related <a
     * href="https://docs.opencv.org/3.4/d3/dc0/group__imgproc__shape.html#ga17ed9f5d79ae97bd4c7cf18403e1689a">OpenCV
     * documentation</a>.
     *
     * <p>No approximation occurs of the contours' points.
     *
     * @param object the object whose contours should be found.
     * @return a newly created list, containing the extreme outer contours.
     * @throws OperationFailedException if the countour cannot be calculated.
     */
    public static List<Contour> contoursForObject(ObjectMask object)
            throws OperationFailedException {

        CVInit.blockUntilLoaded();

        try {
            // We deep-copy the object, as its voxels are modified by the algorithm according to
            // OpenCV docs
            // https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
            Mat mat = ConvertToMat.fromObject(object.duplicate());

            List<MatOfPoint> contours = new LinkedList<>();
            Imgproc.findContours(
                    mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

            return convertMatOfPoint(contours, object.boundingBox().cornerMin());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Convert <i>many</i> OpenCV matrices describing points to {@code List<Contour>}. */
    private static List<Contour> convertMatOfPoint(
            List<MatOfPoint> matrices, ReadableTuple3i cornerMin) {
        return FunctionalList.mapToList(
                matrices, points -> CVFindContours.createContour(points, cornerMin));
    }

    /** Convert a <i>single</i> OpenCV matrix describing points to a {@link Contour}. */
    private static Contour createContour(MatOfPoint matrix, ReadableTuple3i cornerMin) {
        Contour contour = new Contour();

        Arrays.stream(matrix.toArray())
                .map(point -> convert(point, cornerMin))
                .forEach(contour.getPoints()::add);

        return contour;
    }

    /** Converts a 2D OpenCV {@link Point} to a {@link Point3f}, adding {@code toAdd}. */
    private static Point3f convert(Point point, ReadableTuple3i toAdd) {
        return new Point3f(
                addDoubles(point.x, toAdd.x()), addDoubles(point.y, toAdd.y()), toAdd.z());
    }

    /** Adds two {@code double} values and converts to a {@code float}. */
    private static float addDoubles(double value1, double value2) {
        return (float) (value1 + value2);
    }
}
