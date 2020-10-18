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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.core.object.scale.ScaledElements;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.extent.scale.ScaleFactor;

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

    public void assertExpectedArea(ObjectCollection unscaled, ScaledElements<ObjectMask> scaled) {
        assertExpectedArea(
                totalArea(unscaled.asList()), totalArea(scaled.asCollectionOrderNotPreserved()));
    }

    public void assertExpectedArea(int sizeUnscaled, int sizeScaled) {
        // Compare sizes within some tolerance
        assertEquals(
                "area",
                sizeUnscaled * Math.pow(scaleFactor, 2),
                sizeScaled,
                sizeScaled * TOLERANCE_RATIO_SIZE);
    }

    private static int totalArea(Collection<ObjectMask> objects) {
        return objects.stream().mapToInt(ObjectMask::numberVoxelsOn).sum();
    }
}
