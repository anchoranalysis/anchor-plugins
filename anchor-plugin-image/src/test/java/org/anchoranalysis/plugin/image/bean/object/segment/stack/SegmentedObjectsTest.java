/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SegmentedObjects}.
 *
 * @author Owen Feehan
 */
class SegmentedObjectsTest {

    private static final Extent EXTENT = new Extent(800, 600, 20);

    private static final double SCALE_FACTOR = 2.0;

    @Test
    void testMap() throws OperationFailedException {
        SegmentedObjects objects = SegmentedObjectsFixture.create(true, true);

        List<Integer> sizeUnscaled = calcuateNumberVoxels(objects);

        SegmentedObjects objectsScaled = objects.scale(new ScaleFactor(SCALE_FACTOR), EXTENT);

        List<Integer> sizeScaled = calcuateNumberVoxels(objectsScaled);
        assertSizeRatioApprox(sizeScaled, sizeUnscaled, Math.pow(2.0, SCALE_FACTOR));
    }

    /** Creates a list with the number of objects for each respective object (preserving order). */
    private static List<Integer> calcuateNumberVoxels(SegmentedObjects objects) {
        return objects.asObjects().stream().mapToList(ObjectMask::numberVoxelsOn);
    }

    /**
     * Asserts that the ratio of each element in {@code sizeScaled} to the respective element in
     * {@code sizeUnscaled} is approxiamtely {@code approximateRatio}.
     */
    private static void assertSizeRatioApprox(
            List<Integer> sizeScaled, List<Integer> sizeUnscaled, double approximateRatio) {
        assert (sizeScaled.size() == sizeUnscaled.size());
        for (int i = 0; i < sizeScaled.size(); i++) {
            double sizeRatio = ((double) sizeScaled.get(i)) / sizeUnscaled.get(i);
            assertEquals(sizeRatio, approximateRatio, 0.1);
        }
    }
}
