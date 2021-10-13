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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.ImgLib2Lanczos;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.box.DrawObjectOnStackGenerator;
import org.anchoranalysis.image.io.stack.output.box.ScaleableBackground;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import org.anchoranalysis.spatial.box.BoundedList;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Create a thumbnail by drawing an outline of an object at a particular-scale, and placing it
 * centered in a window of a certain size.
 *
 * <p>Preserves the relative-size between objects (i.e. they are all reduced by the same
 * scale-factor) in the same batch.
 *
 * <p>If it's a z-stack, a maximum intensity projection is first applied.
 *
 * <p>All thumbnails are created with identical size. An error will occur if the background is ever
 * smaller than the thumbnail size.
 *
 * <p>If no specific background-channel is set with {@link #setBackgroundChannelIndex} then the
 * following scheme applies:
 *
 * <ul>
 *   <li>If {@code backgroundSource} has exactly zero channels, an empty zero-valued monochrome
 *       background is used (unsigned 8 bit).
 *   <li>If {@code backgroundSource} has exactly one channel, it's used as a monochrome background
 *   <li>If {@code backgroundSource} has exactly three channels, it's used as a RGB background
 *   <li>If {@code backgroundSource} has any other number of channels, the first channel is used as
 *       a background.
 * </ul>
 *
 * @author Owen Feehan
 */
public class OutlinePreserveRelativeSize extends ThumbnailFromObjects {

    // START BEAN PROPERTIES
    /** Size of all created thumbnails */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);

    /**
     * Uses only this channel (identified by an index in the stack) as the background, -1 disables.
     */
    @BeanField @Getter @Setter private int backgroundChannelIndex = -1;

    /** Interpolator used when scaling the background */
    @BeanField @Getter @Setter private InterpolatorBean interpolator = new ImgLib2Lanczos();

    /**
     * The width of the outline. By default, it's 3 as it's nice to have a strongly easily-visible
     * emphasis on where the object is in a thumbnail.
     */
    @BeanField @Getter @Setter private int outlineWidth = 3;

    /**
     * Optionally outline the other (unselected for the thumbnail) objects in this particular color.
     * If not set, these objects aren't outlined at all..
     */
    @BeanField @OptionalBean @Getter @Setter
    private RGBColorBean colorUnselectedObjects = new RGBColorBean(0, 0, 255);
    // END BEAN PROPERTIES

    private class BatchImplementation implements ThumbnailBatch<ObjectCollection> {

        private final DrawObjectOnStackGenerator generator;
        private final FlattenAndScaler scaler;
        private final Extent sceneExtentScaled;

        /**
         * Sets up the generator and the related {@code sceneExtentScaled} variable
         *
         * @param objectsUnscaled unscaled objects
         * @param backgroundScaled scaled background if it exists
         */
        public BatchImplementation(
                FlattenAndScaler scaler,
                ObjectCollection objectsUnscaled,
                Optional<ScaleableBackground> backgroundScaled) {
            this.scaler = scaler;

            this.sceneExtentScaled =
                    scaler.extentFromStackOrObjects(backgroundScaled, objectsUnscaled);

            // Create a generator that draws objects on the background
            this.generator =
                    DrawObjectOnStackGenerator.createFromStack(
                            backgroundScaled, outlineWidth, createColorIndex(false));
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
                        .anyDimensionIsLargerThan(size.asExtent()));

                // Find a bounding-box of target size in which objectScaled is centered
                BoundingBox centeredBox =
                        CenterBoundingBoxHelper.deriveCenteredBoxWithSize(
                                objectsScaled.boundingBox(), size.asExtent(), sceneExtentScaled);

                assert centeredBox.extent().equals(size.asExtent());
                assert sceneExtentScaled.contains(centeredBox);

                Stack transformed =
                        generator.transform(
                                determineObjectsForGenerator(objectsScaled, centeredBox));
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
            if (colorUnselectedObjects != null) {
                return objectsMapped.addObjectsNoBoundingBoxChange(
                        scaler.objectsThatIntersectWith(
                                        objectsMapped.boundingBox(),
                                        ObjectCollectionFactory.of(objectsScaled.list()))
                                .asList());
            } else {
                return objectsMapped;
            }
        }

        /**
         * Creates a suitable color index for distinguishing between the different types of objects
         * that appear
         *
         * @param pairs whether pairs are being used or not
         * @return the color index
         */
        private ColorIndex createColorIndex(boolean pairs) {
            return new ThumbnailColorIndex(pairs, colorUnselectedObjects.toAWTColor());
        }
    }

    @Override
    public ThumbnailBatch<ObjectCollection> start(
            ObjectCollection objects,
            StreamableCollection<BoundingBox> boundingBoxes,
            Optional<Stack> backgroundSource)
            throws OperationFailedException {

        if (!objects.isEmpty()) {
            Interpolator interpolatorBackground = interpolator.create();

            // Determine what to scale the objects and any background by
            FlattenAndScaler scaler =
                    new FlattenAndScaler(
                            boundingBoxes, objects, interpolatorBackground, size.asExtent());

            return new BatchImplementation(
                    scaler,
                    objects,
                    determineBackgroundMaybeOutlined(
                            backgroundSource, scaler, interpolatorBackground));
        } else {
            return objectForBatch -> {
                throw new CreateException("No objects are expected in this batch");
            };
        }
    }

    private Optional<ScaleableBackground> determineBackgroundMaybeOutlined(
            Optional<Stack> backgroundSource, FlattenAndScaler scaler, Interpolator interpolator)
            throws OperationFailedException {
        BackgroundSelector backgroundHelper =
                new BackgroundSelector(
                        backgroundChannelIndex, scaler.getScaleFactor(), interpolator);
        return backgroundHelper.determineBackground(backgroundSource);
    }
}
