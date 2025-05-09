/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.object;

import static org.anchoranalysis.plugin.image.object.CheckVolumeBeforeAfter.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point2d;
import org.anchoranalysis.test.image.object.CircleObjectFixture;
import org.junit.jupiter.api.Test;

/**
 * Tests methods on {@link ObjectMask}.
 *
 * <p>It would better belong in {@code anchor-image} but is kept here instead to reuse a fixture.
 *
 * @author Owen Feehan
 */
class ObjectMaskTest {

    /** Expected number of voxels in the intersection of the two circular objects */
    private static final int EXPECTED_NUMBER_VOXELS_INTERSECTION = 241;

    /** How much to scale the objects by (in each of the X and Y dimensions) */
    private static final int SCALE_FACTOR = 7;

    /**
     * Scales up an object-mask with an interpolator
     *
     * @throws OperationFailedException
     */
    @Test
    void testScaleUp() throws OperationFailedException {

        ScaledObjectAreaChecker checker = new ScaledObjectAreaChecker(SCALE_FACTOR);

        // Create an object that is a small circle
        ObjectMask unscaled = CircleObjectFixture.circleAt(new Point2d(8, 8), 7);
        checker.assertConnected("unscaled", unscaled);

        ObjectMask scaled = unscaled.scale(checker.factor());

        checker.assertConnected("scaled", scaled);
        checker.assertExpectedArea(unscaled, scaled);
    }

    @Test
    void testIntersect() {

        ObjectCollection objects = TwoIntersectingCirclesFixture.create();

        // Check the circular objects have the volume that is expected
        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objects, "");

        Optional<ObjectMask> intersection =
                objects.get(0)
                        .intersect(
                                objects.get(1), TwoIntersectingCirclesFixture.DIMENSIONS.extent());

        // Check the circular objects have the same volume as before
        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objects, "");

        assertTrue(intersection.isPresent(), "intersection is defined");
        assertDiscreteVolume(
                "intersection", EXPECTED_NUMBER_VOXELS_INTERSECTION, intersection.get());
    }
}
