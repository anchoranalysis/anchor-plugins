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

package org.anchoranalysis.plugin.ij.bean.object.provider;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

/**
 * Traces out the shortest-path (a line) between two points in an object-mask.
 *
 * <p>This ensures the two points are connected (4/6 neighborhood)
 *
 * @author feehano
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WalkShortestPath {

    /** Walks a 4-connected line between two points, producing a minimal object-mask for both */
    public static ObjectMask walkLine(Point3i point1, Point3i point2)
            throws OperationFailedException {
        return walkLineInternal(Arrays.asList(point1, point2));
    }

    /**
     * Joins a series of points together by drawling a line between each success pair of points
     *
     * @param points the points which define the line
     * @return an object-mask with a line draw between successive points (like a polygon)
     */
    public static ObjectMask walkLine(List<Point3d> points) throws OperationFailedException {

        if (points.size() < 2) {
            throw new OperationFailedException(
                    String.format(
                            "A minimum of two points is requred to walk a line, but there are only %d points.",
                            points.size()));
        }

        return walkLineInternal(PointConverter.convert3i(points));
    }

    private static ObjectMask walkLineInternal(List<Point3i> points)
            throws OperationFailedException {

        checkCoplanar(points);

        BoundingBox bbox = BoundingBoxFromPoints.forList(points);

        ObjectMask object = new ObjectMask(bbox);

        for (int i = 0; i < (points.size() - 1); i++) {

            Point3i point1 = points.get(i);
            Point3i point2 = points.get(i + 1);
            assert (point1.getZ() == point2.getZ());

            drawLineOnVoxelBuffer(
                    object.binaryVoxelBox()
                            .getVoxelBox()
                            .getPixelsForPlane(point1.getZ() - bbox.cornerMin().getZ()),
                    object.binaryVoxelBox().getVoxelBox().extent(),
                    object.binaryVoxelBox().getBinaryValues().createByte(),
                    point1,
                    point2,
                    bbox.cornerMin());
        }

        return object;
    }

    private static void checkCoplanar(List<Point3i> points) throws OperationFailedException {

        Point3i firstPoint = null;

        for (Point3i point : points) {

            if (firstPoint == null) {
                firstPoint = point;
            }

            // Only accept points in 2D
            if (firstPoint.getZ() != point.getZ()) {
                throw new OperationFailedException(
                        String.format(
                                "the first point in the list (%d) and another point (%d) have different z values. This algorithm only supports co-planar points (parallel to XY-axis)",
                                firstPoint.getZ(), point.getZ()));
            }
        }
    }

    private static void drawLineOnVoxelBuffer(
            VoxelBuffer<ByteBuffer> plane,
            Extent extent,
            BinaryValuesByte bvb,
            Point3i point1,
            Point3i point2,
            ReadableTuple3i cornerMin) {
        drawLine4(
                plane,
                extent,
                bvb,
                point1.getX() - cornerMin.getX(),
                point1.getY() - cornerMin.getY(),
                point2.getX() - cornerMin.getX(),
                point2.getY() - cornerMin.getY());
    }

    /*
     * From ImageProcessor.java in ImageJ
     *
     * Draws a line using the Bresenham's algorithm that is 4-connected instead of 8-connected.<br>
    Based on code from http://stackoverflow.com/questions/5186939/algorithm-for-drawing-a-4-connected-line<br>
    Author: Gabriel Landini (G.Landini at bham.ac.uk)
     */
    private static void drawLine4(
            VoxelBuffer<ByteBuffer> plane,
            Extent extent,
            BinaryValuesByte bvb,
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
            drawPoint(plane, extent, bvb, x1, y1);

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
        drawPoint(plane, extent, bvb, x2, y2);
    }

    private static int sgn(int x1, int x2) {
        return x1 < x2 ? 1 : -1;
    }

    private static void drawPoint(
            VoxelBuffer<ByteBuffer> plane, Extent extent, BinaryValuesByte bvb, int x, int y) {
        plane.putByte(extent.offset(x, y), bvb.getOnByte());
    }
}
