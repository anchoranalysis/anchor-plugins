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

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.image.inference.bean.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.image.inference.bean.segment.reduce.ReduceElements;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SegmentedObjects}.
 *
 * @author Owen Feehan
 */
class SegmentedObjectsTest {

    private static final int NUMBER_OBJECTS_BEFORE_REDUCTION = 10;

    private static final int NUMBER_OBJECTS_AFTER_REDUCTION = 6;

    private static final ReduceElements<ObjectMask> REDUCE = new RemoveOverlappingObjects();

    @Test
    void testReduceCollective() throws OperationFailedException {
        doTest(false);
    }

    @Test
    void testReduceByLabel() throws OperationFailedException {
        doTest(true);
    }

    private void doTest(boolean separateEachLabel) throws OperationFailedException {
        SegmentedObjects objects = SegmentedObjectsFixture.create(true, true);

        SegmentedObjects reduced =
                objects.reduce(REDUCE, separateEachLabel, ExecutionTimeRecorderIgnore.instance());

        assertEquals(NUMBER_OBJECTS_BEFORE_REDUCTION, objects.size());
        assertEquals(NUMBER_OBJECTS_AFTER_REDUCTION, reduced.size());
    }
}
