package org.anchoranalysis.plugin.image.bean.object;

import static org.junit.Assert.assertEquals;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.object.ObjectMask;

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

        assertVolumeEquals(objectIdentifier, expectedNumberVoxels, object, tolerance);
    }

    public static void assertDiscreteVolume(
            String objectIdentifier, int expectedNumberVoxels, ObjectMask object) {
        assertVolumeEquals(objectIdentifier, expectedNumberVoxels, object, 0);
    }

    private static void assertVolumeEquals(
            String message, double expectedNumberVoxels, ObjectMask object, double tolerance) {
        assertEquals(message, expectedNumberVoxels, object.numberVoxelsOn(), tolerance);
    }

    private static double areaOfCircle(int radius) {
        return radius * radius * Math.PI;
    }
}
