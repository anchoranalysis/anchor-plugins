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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.mpp.mark.points.RotatableBoundingBox;
import org.anchoranalysis.spatial.orientation.Orientation2D;
import org.anchoranalysis.spatial.orientation.RotationMatrix;
import org.anchoranalysis.spatial.point.Point2d;
import org.anchoranalysis.spatial.point.Point2f;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * Extracts a bounding box from arrays returned by the EAST CNN model.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class RotatableBoundingBoxFromArrays {

    /**
     * Builds a bounding-box from the arrays returned from the EAST algorithm
     *
     * <p>The bounding-boxes are encoded in RBOX format, see the original EAST paper Zhou et al.
     * 2017
     *
     * <p>A C++ example of calling EAST via OpenCV can be found on <a
     * href="https://github.com/opencv/opencv/blob/master/samples/dnn/text_detection.cpp">GitHub</a>
     *
     * @param geometryArrs an array of 5 arrays
     * @param index the current index to look in each of the 5 arrays
     * @param offset an offset in the scene to add to each generated bounding-box
     * @return a mark encapsulating a rotatable bounding-box
     */
    public static RotatableBoundingBox markFor(float[][] geometryArrs, int index, Point2i offset) {

        Point2f startUnrotated =
                new Point2f(
                        geometryArrs[3][index], // distance to left-boundary of box (x-min)
                        geometryArrs[0][index] // distance to top-boundary of box (y-min)
                        );

        Point2f endUnrotated =
                new Point2f(
                        geometryArrs[1][index], // distance to right-boundary of box (x-max)
                        geometryArrs[2][index] // distance to bottom-boundary of box (y-max)
                        );

        // Clockwise angle to rotate around the offset
        float angle = geometryArrs[4][index];

        return markFor(startUnrotated, endUnrotated, angle, offset);
    }

    private static RotatableBoundingBox markFor(
            Point2f startUnrotated, Point2f endUnrotated, float angle, Point2i offset) {

        // Make it counter-clockwise by multiplying by -1
        Orientation2D orientation = new Orientation2D(-1.0 * angle);

        Point3d endRotated = rotatedPointWithOffset(endUnrotated, orientation, offset);

        float width = startUnrotated.x() + endUnrotated.x();
        float height = startUnrotated.y() + endUnrotated.y();

        Point3d midpoint = midPointFor(width, height, angle, endRotated);

        return createMarkFor(midpoint, width, height, orientation);
    }

    private static RotatableBoundingBox createMarkFor(
            Point3d midpoint, float width, float height, Orientation2D orientation) {
        RotatableBoundingBox mark = new RotatableBoundingBox();
        mark.setPosition(midpoint);
        mark.update(
                new Point2d(-width / 2, -height / 2),
                new Point2d(width / 2, height / 2),
                orientation);
        return mark;
    }

    private static Point3d rotatedPointWithOffset(
            Point2f unrotated, Orientation2D orientation, Point2i offset) {

        RotationMatrix rotMatrix = orientation.deriveRotationMatrix();

        Point3d rotated = rotMatrix.rotatedPoint(convert3d(unrotated));
        rotated.add(PointConverter.doubleFromInt(offset));
        return rotated;
    }

    private static Point3d midPointFor(float width, float height, float angle, Point3d endRotated) {

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        Point3d heightVector = new Point3d(-sinA * height, -cosA * height, 0);
        Point3d widthVector = new Point3d(-cosA * width, sinA * width, 0);

        // Average point between the two
        Point3d avg = meanPoint(heightVector, widthVector);
        avg.add(endRotated);
        return avg;
    }

    private static Point3d meanPoint(Point3d point1, Point3d point2) {
        return new Point3d((point1.x() + point2.x()) / 2, (point1.y() + point2.y()) / 2, 0);
    }

    public static Point3d convert3d(Point2f p) {
        return new Point3d(p.x(), p.y(), 0);
    }
}
