/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.object.provider;

import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Traces out the shortest-path (a line) between two or more points in an object-mask.
 *
 * <p>This ensures the points are connected (4/6 neighborhood)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WalkShortestPath {

    /**
     * Walks a 4-connected line between two points, producing a minimal object-mask for both.
     *
     * @param point1 the first point
     * @param point2 the second point
     * @return an {@link ObjectMask} containing the line between the two points
     * @throws OperationFailedException if the operation fails
     */
    public static ObjectMask walkLine(Point3i point1, Point3i point2)
            throws OperationFailedException {
        return walkLineInternal(Arrays.asList(point1, point2));
    }

    /**
     * Joins a series of points together by drawing a line between each successive pair of points.
     *
     * @param points the points which define the line
     * @return an {@link ObjectMask} with a line drawn between successive points (like a polygon)
     * @throws OperationFailedException if fewer than two points are provided or points are not
     *     coplanar
     */
    public static ObjectMask walkLine(List<Point3d> points) throws OperationFailedException {

        if (points.size() < 2) {
            throw new OperationFailedException(
                    String.format(
                            "A minimum of two points is required to walk a line, but there are only %d points.",
                            points.size()));
        }

        return walkLineInternal(PointConverter.convert3i(points));
    }

    /**
     * Internal method to walk a line between a list of integer points.
     *
     * @param points the list of points to connect
     * @return an {@link ObjectMask} containing the connected line
     * @throws OperationFailedException if the points are not coplanar
     */
    private static ObjectMask walkLineInternal(List<Point3i> points)
            throws OperationFailedException {

        checkCoplanar(points);

        BoundingBox box = BoundingBoxFromPoints.fromStream(points.stream());

        ObjectMask object = new ObjectMask(box);

        for (int i = 0; i < (points.size() - 1); i++) {

            Point3i point1 = points.get(i);
            Point3i point2 = points.get(i + 1);
            assert (point1.z() == point2.z());

            drawLineOnVoxelBuffer(
                    object.binaryVoxels().slice(point1.z() - box.cornerMin().z()),
                    object.binaryVoxels().extent(),
                    object.binaryVoxels().binaryValues().asByte(),
                    point1,
                    point2,
                    box.cornerMin());
        }

        return object;
    }

    /**
     * Checks if all points in the list are coplanar (have the same z-coordinate).
     *
     * @param points the list of points to check
     * @throws OperationFailedException if the points are not coplanar
     */
    private static void checkCoplanar(List<Point3i> points) throws OperationFailedException {

        Point3i firstPoint = null;

        for (Point3i point : points) {

            if (firstPoint == null) {
                firstPoint = point;
            }

            // Only accept points in 2D
            if (firstPoint.z() != point.z()) {
                throw new OperationFailedException(
                        String.format(
                                "The first point in the list (%d) and another point (%d) have different z values. This algorithm only supports co-planar points (parallel to XY-axis)",
                                firstPoint.z(), point.z()));
            }
        }
    }

    /**
     * Draws a line on a voxel buffer between two points.
     *
     * @param plane the voxel buffer to draw on
     * @param extent the extent of the voxel buffer
     * @param binaryValues the binary values to use for drawing
     * @param point1 the first point
     * @param point2 the second point
     * @param cornerMin the minimum corner of the bounding box
     */
    private static void drawLineOnVoxelBuffer(
            VoxelBuffer<UnsignedByteBuffer> plane,
            Extent extent,
            BinaryValuesByte binaryValues,
            Point3i point1,
            Point3i point2,
            ReadableTuple3i cornerMin) {
        drawLine4(
                plane,
                extent,
                binaryValues,
                point1.x() - cornerMin.x(),
                point1.y() - cornerMin.y(),
                point2.x() - cornerMin.x(),
                point2.y() - cornerMin.y());
    }

    /**
     * Draws a 4-connected line using Bresenham's algorithm.
     *
     * @param plane the voxel buffer to draw on
     * @param extent the extent of the voxel buffer
     * @param binaryValues the binary values to use for drawing
     * @param x1 x-coordinate of the first point
     * @param y1 y-coordinate of the first point
     * @param x2 x-coordinate of the second point
     * @param y2 y-coordinate of the second point
     */
    private static void drawLine4(
            VoxelBuffer<UnsignedByteBuffer> plane,
            Extent extent,
            BinaryValuesByte binaryValues,
            int x1,
            int y1,
            int x2,
            int y2) {

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sgnX = sgn(x1, x2);
        int sgnY = sgn(y1, y2);
        int e = 0;
        for (int i = 0; i < dx + dy; i++) {
            drawPoint(plane, extent, binaryValues, x1, y1);

            int e1 = e + dy;
            int e2 = e - dx;
            if (Math.abs(e1) < Math.abs(e2)) {
                x1 += sgnX;
                e = e1;
            } else {
                y1 += sgnY;
                e = e2;
            }
        }
        drawPoint(plane, extent, binaryValues, x2, y2);
    }

    /**
     * Returns the sign of the difference between two integers.
     *
     * @param x1 the first integer
     * @param x2 the second integer
     * @return 1 if x1 < x2, -1 otherwise
     */
    private static int sgn(int x1, int x2) {
        return x1 < x2 ? 1 : -1;
    }

    /**
     * Draws a single point on the voxel buffer.
     *
     * @param plane the voxel buffer to draw on
     * @param extent the extent of the voxel buffer
     * @param binaryValues the binary values to use for drawing
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     */
    private static void drawPoint(
            VoxelBuffer<UnsignedByteBuffer> plane,
            Extent extent,
            BinaryValuesByte binaryValues,
            int x,
            int y) {
        plane.putByte(extent.offset(x, y), binaryValues.getOn());
    }
}
