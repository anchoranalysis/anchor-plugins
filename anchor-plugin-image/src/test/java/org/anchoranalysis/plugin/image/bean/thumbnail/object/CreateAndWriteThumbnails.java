/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2023 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
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
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;

/** Creates thumbnails from a collection of objects, and writes into a directory. */
@NoArgsConstructor
class CreateAndWriteThumbnails {

    /** The background stack used for creating thumbnails. */
    private static final Stack BACKGROUND = EnergyStackFixture.create(true, false).asStack();

    /**
     * Creates thumbnails from a collection of objects, and writes each thumbnail into a directory.
     *
     * @param writer the {@link WriteThumbnailsIntoDirectory} to use for writing thumbnails
     * @param objects the {@link ObjectCollection} to create thumbnails from
     * @param size the {@link SizeXY} of the thumbnails
     * @param overlappingObjects whether the objects can overlap
     * @return a {@link List} of {@link DisplayStack} representing the created thumbnails
     * @throws OperationFailedException if the operation fails
     */
    public static List<DisplayStack> apply(
            WriteThumbnailsIntoDirectory writer,
            ObjectCollection objects,
            SizeXY size,
            boolean overlappingObjects)
            throws OperationFailedException {
        OutlinePreserveRelativeSize outline = createOutline(size, overlappingObjects);
        ThumbnailBatch<ObjectCollection> batch =
                outline.start(
                        objects,
                        boundingBoxes(objects),
                        Optional.of(BACKGROUND),
                        ExecutionTimeRecorderIgnore.instance());

        try {
            List<DisplayStack> thumbnails = thumbnailsFor(batch, objects);
            writer.writeThumbnails(thumbnails);
            return thumbnails;
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates the thumbnails for a particular batch.
     *
     * @param batch the {@link ThumbnailBatch} to create thumbnails from
     * @param objects the {@link ObjectCollection} to create thumbnails for
     * @return a {@link List} of {@link DisplayStack} representing the created thumbnails
     * @throws CreateException if thumbnail creation fails
     */
    private static List<DisplayStack> thumbnailsFor(
            ThumbnailBatch<ObjectCollection> batch, ObjectCollection objects)
            throws CreateException {
        return objects.stream()
                .mapToList(object -> batch.thumbnailFor(ObjectCollectionFactory.of(object)));
    }

    /**
     * Derives the bounding-boxes from a corresponding {@link ObjectCollection}.
     *
     * @param objects the {@link ObjectCollection} to derive bounding boxes from
     * @return a {@link StreamableCollection} of {@link BoundingBox}
     */
    private static StreamableCollection<BoundingBox> boundingBoxes(ObjectCollection objects) {
        return new StreamableCollection<>(
                () -> objects.streamStandardJava().map(ObjectMask::boundingBox));
    }

    /**
     * Creates the {@link OutlinePreserveRelativeSize} bean.
     *
     * @param size the {@link SizeXY} of the thumbnails
     * @param overlappingObjects whether the objects can overlap
     * @return the configured {@link OutlinePreserveRelativeSize} bean
     */
    private static OutlinePreserveRelativeSize createOutline(
            SizeXY size, boolean overlappingObjects) {
        OutlinePreserveRelativeSize outline = new OutlinePreserveRelativeSize();
        outline.setColorUnselectedObjects(new RGBColorBean(Color.BLUE));
        outline.setOutlineWidth(1);
        outline.setSize(size);
        outline.setOverlappingObjects(overlappingObjects);
        outline.setInterpolator(new ImgLib2Lanczos());
        BeanInstanceMapFixture.check(outline);
        return outline;
    }
}
