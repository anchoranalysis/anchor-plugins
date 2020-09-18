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
package org.anchoranalysis.plugin.opencv.nonmaxima;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.combine.ObjectMaskMerger;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ReduceElements;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.test.image.WriteIntoFolder;
import org.anchoranalysis.test.image.object.CircleObjectFixture;

/**
 * Tests a reduce-routine on a number of intersecting circles.
 *
 * @author Owen Feehan
 */
class ReduceElementsTester {

    private static final int NUMBER_CIRCLES = 7;

    private final Optional<WriteIntoFolder> writeIntoFolder;

    public ReduceElementsTester() {
        this.writeIntoFolder = Optional.empty();
    }

    public ReduceElementsTester(WriteIntoFolder writeIntoFolder) {
        this.writeIntoFolder = Optional.of(writeIntoFolder);
    }

    public void test(
            ReduceElements<ObjectMask> reduce,
            boolean highestConfidenceObjectUnchanged,
            int numberObjectsAfter,
            double highestConfidence)
            throws OperationFailedException {

        SegmentedObjects segments = createOverlappingCircles();

        SegmentedObjects reduced = new SegmentedObjects(reduce.reduce(segments.asList()));
        
        writeIntoFolder.ifPresent(folder -> writeRasters(folder, segments, reduced));

        assertEquals(
                "identical number of voxels",
                countTotalVoxels(segments),
                countTotalVoxels(reduced));
        assertEquals("number-objects-after", numberObjectsAfter, reduced.asList().size());

        assertTrue(
                "highest confidence object unchanged",
                segments.highestConfidence().equals(reduced.highestConfidence())
                        == highestConfidenceObjectUnchanged);
        assertEquals(
                "highest confidence",
                highestConfidence,
                reduced.highestConfidence().get().getConfidence(),
                1e-3);
    }

    /** Writes raster-images (for debugging) to the filesystem of before and after the reduction. */
    private static void writeRasters(
            WriteIntoFolder write, SegmentedObjects segments, SegmentedObjects reduced) {
        write.writeObjects("before", segments.asObjects());
        write.writeObjects("after", reduced.asObjects());
    }

    private static SegmentedObjects createOverlappingCircles() {
        ObjectCollection circles =
                CircleObjectFixture.successiveCircles(
                        NUMBER_CIRCLES, new Point2d(15, 15), 5, new Point2d(8, 8), 1);

        // Add confidence successively from 0.2 (inclusive) to 0.8 (inclusive) in 0.1 increments
        List<WithConfidence<ObjectMask>> list =
                FunctionalList.mapToListWithIndex(
                        circles.asList(),
                        (object, index) -> new WithConfidence<>(object, (index * 0.1) + 0.2));

        return new SegmentedObjects(list);
    }

    private static int countTotalVoxels(SegmentedObjects segments) {
        try {
            return ObjectMaskMerger.merge(segments.asObjects()).numberVoxelsOn();
        } catch (OperationFailedException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
    }
}
