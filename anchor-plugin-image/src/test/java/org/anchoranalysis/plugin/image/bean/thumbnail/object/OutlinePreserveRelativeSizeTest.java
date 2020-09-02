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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.WriteIntoFolder;
import org.junit.Rule;
import org.junit.Test;

public class OutlinePreserveRelativeSizeTest {

    private static final SizeXY SIZE = new SizeXY(300, 200);

    private static final int NUMBER_INTERSECTING = 4;
    private static final int NUMBER_NOT_INTERSECTING = 2;

    private static final ObjectCollection OBJECTS =
            IntersectingCircleObjectsFixture.generateIntersectingObjects(
                    NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, false);

    private static final Stack BACKGROUND = EnergyStackFixture.create(true, false).asStack();

    @Rule public WriteIntoFolder writer = new WriteIntoFolder(false);

    @Test
    public void testThumbnails() throws OperationFailedException, CreateException {

        List<DisplayStack> thumbnails = createAndWriteThumbnails();

        assertEquals(
                "number of thumbnails",
                thumbnails.size(),
                NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING);
        for (DisplayStack thumbnail : thumbnails) {
            assertEquals("size of a thumbnail", SIZE.asExtent(), thumbnail.extent());
        }

        DualComparer comparer =
                DualComparerFactory.compareTemporaryFolderToTest(
                        writer.getFolder(), "thumbnails", "thumbnails01");
        assertTrue(
                "thumbnails are identical to saved copy", comparer.compareTwoSubdirectories("."));
    }

    /**
     * Creates thumbnails and writes them to the temporary folder
     *
     * @throws OperationFailedException
     */
    private List<DisplayStack> createAndWriteThumbnails() throws OperationFailedException {
        OutlinePreserveRelativeSize outline = createOutline();
        ThumbnailBatch<ObjectCollection> batch = outline.start(OBJECTS, boundingBoxes(OBJECTS), Optional.of(BACKGROUND));

        try {
            List<DisplayStack> thumbnails = thumbnailsFor(batch, OBJECTS);
            writer.writeList("thumbnails", thumbnails);
            return thumbnails;
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<DisplayStack> thumbnailsFor(
            ThumbnailBatch<ObjectCollection> batch, ObjectCollection objects) throws CreateException {
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
        outline.setInterpolator(new InterpolatorBeanLanczos());
        return outline;
    }
}
