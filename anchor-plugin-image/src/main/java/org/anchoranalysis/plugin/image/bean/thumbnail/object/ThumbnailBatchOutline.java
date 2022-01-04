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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.awt.Color;
import java.util.Optional;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.box.DrawObjectOnStackGenerator;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.spatial.box.BoundedList;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * A batch of thumbnails whose outlines will be drawn in {@link OutlinePreserveRelativeSize}.
 *
 * @author Owen Feehan
 */
class ThumbnailBatchOutline implements ThumbnailBatch<ObjectCollection> {

    private final DrawObjectOnStackGenerator generator;
    private final FlattenAndScaler scaler;
    private final Extent sceneSizeScaled;
    private final Extent sceneSizeUnscaled;
    private final Optional<Color> colorUnselectedObjects;
    private final ExecutionTimeRecorder recorder;

    /**
     * Sets up the generator and the related {@code sceneExtentScaled} variable.
     *
     * @param scaler for scaling objects from their unscaled size to their scaled size, and also
     *     flattening in the Z dimension.
     * @param objectsUnscaled unscaled objects.
     * @param sceneSizeUnscaled the size of the scene (unscaled).
     * @param outlineWidth how strong to draw the outline of objects in the thumbnails.
     * @param colorUnselectedObjects what color to draw the outline of unselected objects for. If
     *     {@link Optional#empty()}, these objects are not drawn at all.
     * @param recorder records the execution-time of particular operations.
     */
    public ThumbnailBatchOutline(
            FlattenAndScaler scaler,
            ObjectCollection objectsUnscaled,
            Extent sceneSizeUnscaled,
            int outlineWidth,
            Optional<Color> colorUnselectedObjects,
            ExecutionTimeRecorder recorder) {
        this.scaler = scaler;

        this.colorUnselectedObjects = colorUnselectedObjects;

        this.sceneSizeUnscaled = sceneSizeUnscaled;

        this.sceneSizeScaled = scaler.extentFromStackOrObjects(objectsUnscaled);

        this.recorder = recorder;

        // Create a generator that draws objects on the background
        this.generator =
                DrawObjectOnStackGenerator.createFromStack(
                        scaler.getBackground(), outlineWidth, createColorIndex(false));
    }

    @Override
    public DisplayStack thumbnailFor(ObjectCollection element) throws CreateException {
        // For now only work with the first object in the collection
        try {
            BoundedList<ObjectMask> objectsScaled =
                    BoundedList.createFromList(
                            scaler.scaleObjects(element).asList(), ObjectMask::boundingBox);

            assert (!objectsScaled
                    .boundingBox()
                    .extent()
                    .anyDimensionIsLargerThan(sceneSizeUnscaled));

            // Find a bounding-box of target size in which objectScaled is centered
            BoundingBox centeredBox =
                    CenterBoundingBoxHelper.deriveCenteredBoxWithSize(
                            objectsScaled.boundingBox(), sceneSizeUnscaled, sceneSizeScaled);

            assert centeredBox.extent().equals(sceneSizeUnscaled);
            assert sceneSizeScaled.contains(centeredBox);

            BoundedList<ObjectMask> objectsToDisplay =
                    determineObjectsForGenerator(objectsScaled, centeredBox);
            Stack transformed =
                    recorder.recordExecutionTime(
                            "Transforming thumbnail objects",
                            () -> generator.transform(objectsToDisplay));
            return DisplayStack.create(transformed);

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private BoundedList<ObjectMask> determineObjectsForGenerator(
            BoundedList<ObjectMask> objectsScaled, BoundingBox centeredBox) {
        BoundedList<ObjectMask> objectsMapped = objectsScaled.assignBoundingBox(centeredBox);

        // Add any other objects which intersect with the scaled-bounding box, excluding
        //  the object themselves
        if (colorUnselectedObjects.isPresent()) {
            ObjectCollection intersectingObjects =
                    scaler.objectsThatIntersectWith(
                            objectsMapped.boundingBox(),
                            ObjectCollectionFactory.of(objectsScaled.list()));

            return objectsMapped.addObjectsNoBoundingBoxChange(intersectingObjects.asList());
        } else {
            return objectsMapped;
        }
    }

    /**
     * Creates a suitable color index for distinguishing between the different types of objects that
     * appear.
     *
     * @param pairs whether pairs are being used or not.
     * @return the color index.
     */
    private ColorIndex createColorIndex(boolean pairs) {
        // In practice this color should never be used if it doesn't exist, so PINK is arbitrary.
        return new ThumbnailColorIndex(pairs, colorUnselectedObjects.orElse(Color.PINK));
    }
}
