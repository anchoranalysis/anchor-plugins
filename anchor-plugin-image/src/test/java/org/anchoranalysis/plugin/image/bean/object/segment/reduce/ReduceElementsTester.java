/*-
 * #%L
 * anchor-plugin-opencv
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
package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.inference.bean.segment.reduce.ReduceElements;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjectsFixture;
import org.anchoranalysis.test.image.WriteIntoDirectory;

/**
 * Tests a reduce-routine on a number of intersecting circles.
 *
 * @author Owen Feehan
 */
class ReduceElementsTester {

    private final Optional<WriteIntoDirectory> writeIntoDirectory;

    public ReduceElementsTester() {
        this.writeIntoDirectory = Optional.empty();
    }

    public ReduceElementsTester(WriteIntoDirectory writeIntoDirectory) {
        this.writeIntoDirectory = Optional.of(writeIntoDirectory);
    }

    public void test(
            ReduceElements<ObjectMask> reduce,
            boolean highestConfidenceObjectUnchanged,
            int numberObjectsAfter,
            double highestConfidence)
            throws OperationFailedException {

        SegmentedObjects segments = SegmentedObjectsFixture.create(true, false);

        SegmentedObjects reduced = segments.reduce(reduce, true);

        writeIntoDirectory.ifPresent(folder -> writeRasters(folder, segments, reduced));

        assertEquals(
                countTotalVoxels(segments),
                countTotalVoxels(reduced),
                "identical number of voxels");
        assertEquals(numberObjectsAfter, reduced.size(), "number-objects-after");

        assertEquals(
                highestConfidenceObjectUnchanged,
                segments.highestConfidence().equals(reduced.highestConfidence()),
                "highest confidence object unchanged");
        assertEquals(
                highestConfidence,
                reduced.highestConfidence().get().getConfidence(),
                1e-3,
                "highest confidence");
    }

    /** Writes raster-images (for debugging) to the filesystem of before and after the reduction. */
    private static void writeRasters(
            WriteIntoDirectory write, SegmentedObjects segments, SegmentedObjects reduced) {
        write.writeObjects("before", segments.getObjects().atInputScale().objects());
        write.writeObjects("after", reduced.getObjects().atInputScale().objects());
    }

    private static int countTotalVoxels(SegmentedObjects segments) {
        try {
            return ObjectMaskMerger.merge(segments.getObjects().atInputScale().objects())
                    .numberVoxelsOn();
        } catch (OperationFailedException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
    }
}
