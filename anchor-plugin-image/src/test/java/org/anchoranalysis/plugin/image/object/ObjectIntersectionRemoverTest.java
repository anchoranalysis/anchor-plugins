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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.junit.Test;

public class ObjectIntersectionRemoverTest {

    /** Expected number of voxels for the <i>first</i> object after removal of intersection */
    private static final int EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_FIRST = 136;

    /** Expected number of voxels for the <i>second</i> object after removal of intersection */
    private static final int EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_SECOND = 468;

    /**
     * Removes intersecting voxels from two circular-objects and checks volumes are as expected
     *
     * @throws OperationFailedException
     */
    @Test
    public void testRemoveIntersection() throws OperationFailedException {

        // Create several small circles, some intersecting and some not intersecting
        ObjectCollection objectsBefore = TwoIntersectingCirclesFixture.create();

        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objectsBefore, " before");

        // Remove the intersecting voxelts
        ObjectCollection objectsAfter =
                ObjectIntersectionRemover.removeIntersectingVoxels(
                        objectsBefore, TwoIntersectingCirclesFixture.DIMENSIONS, false);

        TwoIntersectingCirclesFixture.checkModifiedVolumesOnCircles(
                objectsAfter,
                "after",
                EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_FIRST,
                EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_SECOND);
    }
}
