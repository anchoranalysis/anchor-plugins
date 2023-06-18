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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.image.bean.interpolator.ImgLib2Lanczos;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link OutlinePreserveRelativeSize}.
 *
 * @author Owen Feehan
 */
class OutlinePreserveRelativeSizeTest {

	private static final SizeXY SIZE = new SizeXY(300, 200);
	
    private static final int NUMBER_INTERSECTING = 4;
    private static final int NUMBER_NOT_INTERSECTING = 2;

    private static final int NUMBER_TOTAL = NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING;

    private static final ObjectCollection OBJECTS =
            IntersectingCircleObjectsFixture.generateIntersectingObjects(
                    NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, false);

    @TempDir Path temporaryDirectory;

    private WriteThumbnailsIntoDirectory writer;

    static {
        BeanInstanceMapFixture.ensureStackWriter(true);
        BeanInstanceMapFixture.ensureStackDisplayer();
    }

    @BeforeEach
    void setup() {
        writer = new WriteThumbnailsIntoDirectory(temporaryDirectory);
    }

    @Test
    void testThumbnails() throws OperationFailedException, CreateException {

        doTestAndAssert(OBJECTS, NUMBER_TOTAL, true);

        // Additionally check that the written thumbnails are identical to saved images in a
        // resources directory.
        writer.assertWrittenThumbnailsIdenticalToResources("thumbnails01");
    }

    /**
     * Deliberately adds an additional object that is copy of an existing object.
     *
     * <p>This means at least one object is fully occluded.
     *
     * <p>The test checks that the total number of expected thumbnails are created, and all of
     * expected size.
     *
     * @throws OperationFailedException
     */
    @Test
    void testOccludedObject() throws OperationFailedException {
        ObjectCollection objectsPlusExtra = appendObject(OBJECTS, OBJECTS.get(0));
        doTestAndAssert(objectsPlusExtra, NUMBER_TOTAL + 1, true);
    }

    private void doTestAndAssert(
            ObjectCollection objects, int expectedNumber, boolean overlappingObjects)
            throws OperationFailedException {
        List<DisplayStack> thumbnails = CreateAndWriteThumbnails.apply(writer, objects, SIZE, overlappingObjects);
        assertThumbnailsEqual(thumbnails, expectedNumber, SIZE.asExtent());
    }

    /** Asserts that a certain number of thumbnails exist, and all have the expected-size. */
    private void assertThumbnailsEqual(
            List<DisplayStack> thumbnails, int expectedNumber, Extent expectedSize) {
        assertEquals(thumbnails.size(), expectedNumber, "number of thumbnails");
        for (DisplayStack thumbnail : thumbnails) {
            assertEquals(expectedSize, thumbnail.extent(), "size of a thumbnail");
        }
    }

    /** Appends an {@link ObjectMask} to the collection. */
    private static ObjectCollection appendObject(ObjectCollection existing, ObjectMask toAppend) {
        ArrayList<ObjectMask> list = new ArrayList<>(existing.size() + 1);
        for (ObjectMask existingObject : existing) {
            list.add(existingObject);
        }
        list.add(toAppend);
        return new ObjectCollection(list);
    }
}
