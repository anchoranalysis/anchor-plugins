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

package org.anchoranalysis.plugin.points.convexhull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.core.points.PointsFromObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point2i;

// Strongly influenced by http://rsb.info.nih.gov/ij/macros/ConvexHull.txt
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvexHullUtilities {

    private static final int DEFAULT_MIN_NUM_POINTS = 5;

    /** Like {@link #convexHull2D(List, int)} but uses a default minimum number of points. */
    public static List<Point2i> convexHull2D(List<Point2i> points) throws OperationFailedException {
        return convexHull2D(points, DEFAULT_MIN_NUM_POINTS);
    }

    /**
     * Apply a convex-hull algorithm to filter a set of points.
     *
     * <p>Note the algorithm will return the input-points if there are too few points.
     *
     * @param points points to filter
     * @param minNumberPoints a minimum of number of points (before any convex hull filtering) that
     *     must be found.
     * @return the filtered points on convex-hull iff the minimum number of points exists, otherwise
     *     the input points unchanged.
     * @throws OperationFailedException if the count variable becomes too high during calculation
     */
    public static List<Point2i> convexHull2D(List<Point2i> points, int minNumberPoints)
            throws OperationFailedException {
        if (points.size() >= minNumberPoints) {
            List<Point2i> filtered = filterPointsGiftWrap(points);

            if (!filtered.isEmpty()) {
                return filtered;
            } else {
                // If there are 0 points, this is considered a failure and the input points are
                // returned
                return points;
            }
        } else {
            // If there are too few input-points, the input points are returned
            return points;
        }
    }

    /** "Gift wrap" algorithm for convex-hull from ImageJ */
    private static List<Point2i> filterPointsGiftWrap(List<Point2i> points) // NOSONAR
            throws OperationFailedException {

        int xbase = 0;
        int ybase = 0;

        int[] xx = new int[points.size()];
        int[] yy = new int[points.size()];
        int n2 = 0;

        int p1 = calculateStartingIndex(points);

        int pstart = p1;
        int p2;
        int p3;

        int count = 0;
        do {
            int x1 = points.get(p1).x();
            int y1 = points.get(p1).y();
            p2 = p1 + 1;

            if (p2 == points.size()) {
                p2 = 0;
            }

            int x2 = points.get(p2).x();
            int y2 = points.get(p2).y();
            p3 = p2 + 1;

            if (p3 == points.size()) {
                p3 = 0;
            }

            int determinate;
            do {
                int x3 = points.get(p3).x();
                int y3 = points.get(p3).y();
                determinate = x1 * (y2 - y3) - y1 * (x2 - x3) + (y3 * x2 - y2 * x3);
                if (determinate > 0) {
                    x2 = x3;
                    y2 = y3;
                    p2 = p3;
                }
                p3 += 1;
                if (p3 == points.size()) p3 = 0;
            } while (p3 != p1);

            if (n2 < points.size()) {
                xx[n2] = xbase + x1;
                yy[n2] = ybase + y1;
                n2++;
            } else {
                count++;
                if (count > 10) {
                    throw new OperationFailedException(
                            "Count variable is too high, cannot calculate convex-hull");
                }
            }

            p1 = p2;

        } while (p1 != pstart);

        // n2 is number of points out
        return createPointsList(n2, xx, yy);
    }

    private static List<Point2i> createPointsList(int numPoints, int[] xx, int[] yy) {
        return IntStream.range(0, numPoints)
                .mapToObj(index -> new Point2i(xx[index], yy[index]))
                .collect(Collectors.toList());
    }

    public static List<Point2i> pointsOnAllOutlines(ObjectCollection objects) {
        return PointsFromObject.listFromAllOutlines2i(objects);
    }

    public static List<Point2i> pointsOnOutline(ObjectMask object) throws CreateException {
        return PointsFromObject.listFromOutline2i(object);
    }

    private static int calculateStartingIndex(List<Point2i> pointsIn) {

        int smallestY = Integer.MAX_VALUE;
        int x;
        int y;
        for (int i = 0; i < pointsIn.size(); i++) {
            y = pointsIn.get(i).y();
            if (y < smallestY) {
                smallestY = y;
            }
        }

        int smallestX = Integer.MAX_VALUE;
        int p1 = 0;
        for (int i = 0; i < pointsIn.size(); i++) {
            x = pointsIn.get(i).x();
            y = pointsIn.get(i).y();
            if (y == smallestY && x < smallestX) {
                smallestX = x;
                p1 = i;
            }
        }
        return p1;
    }
}
