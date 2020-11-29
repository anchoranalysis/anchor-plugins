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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.Contour;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.ReadableTuple3i;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/** Wrapper around Open CV's findContours function */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CVFindContours {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    public static List<Contour> contoursForObject(ObjectMask object)
            throws OperationFailedException {

        CVInit.blockUntilLoaded();

        try {
            // We clone ss the source image is modified by the algorithm according to OpenCV docs
            // https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
            Mat mat = ConvertToMat.fromObject(object.duplicate());

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(
                    mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

            return convertMatOfPoint(contours, object.boundingBox().cornerMin());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<Contour> convertMatOfPoint(
            List<MatOfPoint> contours, ReadableTuple3i cornerMin) {
        return FunctionalList.mapToList(
                contours, points -> CVFindContours.createContour(points, cornerMin));
    }

    private static Contour createContour(MatOfPoint mop, ReadableTuple3i cornerMin) {
        Contour contour = new Contour();

        Arrays.stream(mop.toArray())
                .map(point -> convert(point, cornerMin))
                .forEach(contour.getPoints()::add);

        return contour;
    }

    private static Point3f convert(Point point, ReadableTuple3i cornerMin) {
        return new Point3f(
                convertAdd(point.x, cornerMin.x()),
                convertAdd(point.y, cornerMin.y()),
                cornerMin.z());
    }

    private static float convertAdd(double in, double add) {
        return (float) (in + add);
    }
}
