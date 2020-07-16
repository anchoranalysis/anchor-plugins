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

package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.mark.MarkRotatableBoundingBox;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point2f;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.math.rotation.RotationMatrix;

/**
 * Extracts a bounding box from arrays returned by the EAST deep-CNN model.
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
     * @param bndScene the dimensions of the image to which the bounding-box belongs
     * @return a mark encapsulating a rotatable bounding-box
     */
    public static MarkRotatableBoundingBox markFor(
            float[][] geometryArrs, int index, Point2i offset) {

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

    private static MarkRotatableBoundingBox markFor(
            Point2f startUnrotated, Point2f endUnrotated, float angle, Point2i offset) {

        // Make it counter-clockwise by multiplying by -1
        Orientation2D orientation = new Orientation2D(-1.0 * angle);

        Point3d endRotated = rotatedPointWithoffset(endUnrotated, orientation, offset);

        float width = startUnrotated.getX() + endUnrotated.getX();
        float height = startUnrotated.getY() + endUnrotated.getY();

        Point3d midpoint = midPointFor(width, height, angle, endRotated);

        return createMarkFor(midpoint, width, height, orientation);
    }

    private static MarkRotatableBoundingBox createMarkFor(
            Point3d midpoint, float width, float height, Orientation2D orientation) {
        MarkRotatableBoundingBox mark = new MarkRotatableBoundingBox();
        mark.setPos(midpoint);
        mark.update(
                new Point2d(-width / 2, -height / 2),
                new Point2d(width / 2, height / 2),
                orientation);
        return mark;
    }

    private static Point3d rotatedPointWithoffset(
            Point2f unrotated, Orientation2D orientation, Point2i offset) {

        RotationMatrix rotMatrix = orientation.createRotationMatrix();

        Point3d rotated = rotMatrix.calcRotatedPoint(convert3d(unrotated));
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
        return new Point3d(
                (point1.getX() + point2.getX()) / 2, (point1.getY() + point2.getY()) / 2, 0);
    }

    public static Point3d convert3d(Point2f p) {
        return new Point3d(p.getX(), p.getY(), 0);
    }
}
