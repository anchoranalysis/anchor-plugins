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

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/** Utility class for checking the volume of {@link ObjectMask}s before and after operations. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckVolumeBeforeAfter {

    /**
     * Allows % tolerance in the number of voxels in the area of a voxelized circle relative to the
     * mathematical expected value.
     */
    private static final double RATIO_TOLERANCE_FOR_VOXELIZATION = 0.05;

    /**
     * Asserts that the area of a circular {@link ObjectMask} is within tolerance of the expected
     * area.
     *
     * @param objectIdentifier a string identifier for the object being checked
     * @param expectedRadius the expected radius of the circular object
     * @param object the {@link ObjectMask} to check
     */
    public static void assertCircularArea(
            String objectIdentifier, int expectedRadius, ObjectMask object) {
        double expectedNumberVoxels = areaOfCircle(expectedRadius);

        // Allow % tolerance due to voxelization
        double tolerance = RATIO_TOLERANCE_FOR_VOXELIZATION * expectedNumberVoxels;

        assertVolumeEquals(expectedNumberVoxels, object, tolerance, objectIdentifier);
    }

    /**
     * Asserts that the volume of an {@link ObjectMask} exactly matches the expected number of
     * voxels.
     *
     * @param objectIdentifier a string identifier for the object being checked
     * @param expectedNumberVoxels the expected number of voxels in the object
     * @param object the {@link ObjectMask} to check
     */
    public static void assertDiscreteVolume(
            String objectIdentifier, int expectedNumberVoxels, ObjectMask object) {
        assertVolumeEquals(expectedNumberVoxels, object, 0, objectIdentifier);
    }

    /**
     * Asserts that the volume of an {@link ObjectMask} is within tolerance of the expected volume.
     *
     * @param expectedNumberVoxels the expected number of voxels
     * @param object the {@link ObjectMask} to check
     * @param tolerance the allowed difference between expected and actual volume
     * @param message the message to display if the assertion fails
     */
    private static void assertVolumeEquals(
            double expectedNumberVoxels, ObjectMask object, double tolerance, String message) {
        assertEquals(expectedNumberVoxels, object.numberVoxelsOn(), tolerance, message);
    }

    /**
     * Calculates the area of a circle given its radius.
     *
     * @param radius the radius of the circle
     * @return the area of the circle
     */
    private static double areaOfCircle(int radius) {
        return radius * radius * Math.PI;
    }
}
