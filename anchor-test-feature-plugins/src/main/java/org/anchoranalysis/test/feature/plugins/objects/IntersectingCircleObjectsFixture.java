/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point2i;

/**
 * A fixture for generating collections of intersecting and non-intersecting circular objects.
 *
 * <p>This class provides utility methods to create object collections with specific configurations
 * of intersecting and non-intersecting circles for testing purposes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntersectingCircleObjectsFixture {

    private static final int INITIAL_MARGIN = 5;

    private static final int INITIAL_RADIUS = 5;

    private static final int RADIUS_INCR = 2;

    /**
     * Generates a collection of circular objects, some intersecting and some not intersecting.
     *
     * @param numberIntersecting the number of intersecting circles to produce
     * @param numberNotIntersecting the number of non-intersecting circles to produce
     * @param sameSize if true, all circles have the same radius ({@code INITIAL_RADIUS}); if false,
     *     the radius gradually increments
     * @return an ObjectCollection containing the generated circular objects
     */
    public static ObjectCollection generateIntersectingObjects(
            int numberIntersecting, int numberNotIntersecting, boolean sameSize) {

        RunningCircleCreator running = new RunningCircleCreator(sameSize);

        // Keep on generating circles of centers radius*1.5 apart, so that they intersect
        ObjectCollection first = running.generateMultipleCircles(numberIntersecting, 1.5);

        // Shift another 1.5 to make sure there's no intersection between first set and second
        running.shift(1.5);

        // Now generate at radius 3 apartment, so that they do not intersect
        ObjectCollection second = running.generateMultipleCircles(numberNotIntersecting, 3);

        // Make sure we haven't generated so many we've run out of the scene
        assert (CircleObjectFixture.sceneContains(running.getCenter()));

        return ObjectCollectionFactory.of(first, second);
    }

    /** Increments the center-point (and maybe the radius) as new circles are generated. */
    private static class RunningCircleCreator {

        private boolean sameSize;

        private int radius = INITIAL_RADIUS;

        private Point2i center = new Point2i(INITIAL_MARGIN + radius, INITIAL_MARGIN + radius);

        public RunningCircleCreator(boolean sameSize) {
            super();
            this.sameSize = sameSize;
        }

        public void shift(double factor) {
            int shift = (int) (factor * radius);
            center.incrementX(shift);
            center.incrementY(shift);
        }

        public ObjectCollection generateMultipleCircles(int numCircles, double factor) {
            return ObjectCollectionFactory.fromRepeated(numCircles, () -> generateCircle(factor));
        }

        private ObjectMask generateCircle(double factor) {
            ObjectMask object = CircleObjectFixture.circleAt(center, radius);
            shift(factor);

            if (!sameSize) {
                radius += RADIUS_INCR;
            }

            return object;
        }

        public Point2i getCenter() {
            return center;
        }
    }
}
