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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.ImgLib2Lanczos;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.WriteIntoDirectory;
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

    private static final ObjectCollection OBJECTS =
            IntersectingCircleObjectsFixture.generateIntersectingObjects(
                    NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, false);

    private static final Stack BACKGROUND = EnergyStackFixture.create(true, false).asStack();

    @TempDir Path temporaryDirectory;

    private WriteIntoDirectory writer;

    @BeforeEach
    void setup() {
        writer = new WriteIntoDirectory(temporaryDirectory, false);
    }

    @Test
    void testThumbnails() throws OperationFailedException, CreateException {

        List<DisplayStack> thumbnails = createAndWriteThumbnails();

        assertEquals(
                thumbnails.size(),
                NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING,
                "number of thumbnails");
        for (DisplayStack thumbnail : thumbnails) {
            assertEquals(SIZE.asExtent(), thumbnail.extent(), "size of a thumbnail");
        }

        DualComparer comparer =
                DualComparerFactory.compareTemporaryDirectoryToTest(
                        writer.getDirectory(), Optional.of("thumbnails"), "thumbnails01");
        assertTrue(
                comparer.compareTwoSubdirectories("."), "thumbnails are identical to saved copy");
    }

    /**
     * Creates thumbnails and writes them to the temporary folder
     *
     * @throws OperationFailedException
     */
    private List<DisplayStack> createAndWriteThumbnails() throws OperationFailedException {
        OutlinePreserveRelativeSize outline = createOutline();
        ThumbnailBatch<ObjectCollection> batch =
                outline.start(OBJECTS, boundingBoxes(OBJECTS), Optional.of(BACKGROUND));

        try {
            List<DisplayStack> thumbnails = thumbnailsFor(batch, OBJECTS);
            writer.writeList("thumbnails", thumbnails, true);
            return thumbnails;
        } catch (CreateException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<DisplayStack> thumbnailsFor(
            ThumbnailBatch<ObjectCollection> batch, ObjectCollection objects)
            throws CreateException {
        return objects.stream()
                .mapToList(object -> batch.thumbnailFor(ObjectCollectionFactory.of(object)));
    }

    private static StreamableCollection<BoundingBox> boundingBoxes(ObjectCollection objects) {
        return new StreamableCollection<>(
                () -> objects.streamStandardJava().map(ObjectMask::boundingBox));
    }

    private static OutlinePreserveRelativeSize createOutline() {
        OutlinePreserveRelativeSize outline = new OutlinePreserveRelativeSize();
        outline.setColorUnselectedObjects(new RGBColorBean(Color.BLUE));
        outline.setOutlineWidth(1);
        outline.setSize(SIZE);
        outline.setInterpolator(new ImgLib2Lanczos());
        return outline;
    }
}
