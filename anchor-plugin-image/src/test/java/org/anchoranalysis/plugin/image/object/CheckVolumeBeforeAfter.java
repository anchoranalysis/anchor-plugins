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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckVolumeBeforeAfter {

    /**
     * Allows % tolerance in the number of voxels in the area of a voxelized circle relative to the
     * mathematical expected value
     */
    private static final double RATIO_TOLERANCE_FOR_VOXELIZATION = 0.05;

    public static void assertCircularArea(
            String objectIdentifier, int expectedRadius, ObjectMask object) {
        double expectedNumberVoxels = areaOfCircle(expectedRadius);

        // Allow % toelrance due to voxelization
        double tolerance = RATIO_TOLERANCE_FOR_VOXELIZATION * expectedNumberVoxels;

        assertVolumeEquals(expectedNumberVoxels, object, tolerance, objectIdentifier);
    }

    public static void assertDiscreteVolume(
            String objectIdentifier, int expectedNumberVoxels, ObjectMask object) {
        assertVolumeEquals(expectedNumberVoxels, object, 0, objectIdentifier);
    }

    private static void assertVolumeEquals(
            double expectedNumberVoxels, ObjectMask object, double tolerance, String message) {
        assertEquals(expectedNumberVoxels, object.numberVoxelsOn(), tolerance, message);
    }

    private static double areaOfCircle(int radius) {
        return radius * radius * Math.PI;
    }
}
