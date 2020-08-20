package org.anchoranalysis.plugin.image.bean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ScaledObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;

@RequiredArgsConstructor
class ScaledObjectAreaChecker {

    /**
     * How much the total scaled-size is allowed different from what is mathematically expected
     * (with the difference occuring due to voxelization or any scaling artefacts)
     */
    private static final double TOLERANCE_RATIO_SIZE = 0.01;

    // START REQUIRED ARGUMENTS
    /** How much to scale both the X and Y dimensions by */
    private final int scaleFactor;
    // END REQUIRED ARGUMENTS

    public ScaleFactor factor() {
        return new ScaleFactor(scaleFactor);
    }

    public void assertConnected(String name, ObjectMask object) {
        assertTrue(name + " is connected", object.checkIfConnected());
    }

    public void assertExpectedArea(ObjectMask unscaled, ObjectMask scaled) {
        assertExpectedArea(unscaled.numberVoxelsOn(), scaled.numberVoxelsOn());
    }

    public void assertExpectedArea(ObjectCollection unscaled, ScaledObjectCollection scaled) {
        assertExpectedArea(totalArea(unscaled), totalArea(scaled.asCollectionOrderNotPreserved()));
    }

    public void assertExpectedArea(int sizeUnscaled, int sizeScaled) {
        // Compare sizes within some tolerance
        assertEquals(
                "area",
                sizeUnscaled * Math.pow(scaleFactor, 2),
                sizeScaled,
                sizeScaled * TOLERANCE_RATIO_SIZE);
    }

    private static int totalArea(ObjectCollection objects) {
        return objects.streamStandardJava().mapToInt(ObjectMask::numberVoxelsOn).sum();
    }
}
