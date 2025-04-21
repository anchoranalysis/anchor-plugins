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
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.scale.ScaledElements;
import org.anchoranalysis.image.core.object.scale.Scaler;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.box.ScaleableBackground;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/** Handles flattening and scaling of objects and stacks for thumbnail generation. */
@RequiredArgsConstructor
class FlattenAndScaler {

    // START REQUIRED ARGUMENTS
    /** Scaling-factor to apply to objects and stacks. */
    @Getter private final ScaleFactor scaleFactor;

    /** The resizer used for scaling voxels. */
    private final VoxelsResizer resizer;

    // END REQUIRED ARGUMENTS

    /** A scaled version of each object. */
    private ScaledElements<ObjectMask> objectsScaled;

    /**
     * An efficiently searchable index of the unscaled objects, indexed by their scaled
     * bounding-boxes.
     */
    private IntersectingObjects<ObjectMask> objectsIndexed;

    /** The background scaled to match the scaled-objects. */
    @Getter private Optional<ScaleableBackground> background;

    /** The size of the image when scaled, if it is known. */
    private Optional<Extent> sizeScaled;

    /**
     * Constructor for FlattenAndScaler.
     *
     * @param boundingBoxes supplies a stream of bounding-boxes that specify each unscaled regions
     *     that we will generate thumbnails for.
     * @param allObjects all objects that may appear in the thumbnails.
     * @param overlappingObjects true if objects may overlap unscaled. false if this is never
     *     allowed. This influences whether scaling occurs collectively (to preserve tight borders
     *     between objects), or individually.
     * @param resizer interpolator for scaling stack.
     * @param targetSize the target size which objects will be scaled-down to fit inside.
     * @param backgroundCreator function to create a {@link ScaleableBackground} given a {@link
     *     ScaleFactor}.
     * @throws OperationFailedException if there are too many objects.
     */
    public FlattenAndScaler(
            StreamableCollection<BoundingBox> boundingBoxes,
            ObjectCollection allObjects,
            boolean overlappingObjects,
            VoxelsResizer resizer,
            Extent targetSize,
            CheckedFunction<ScaleFactor, Optional<ScaleableBackground>, OperationFailedException>
                    backgroundCreator)
            throws OperationFailedException {
        this.scaleFactor = ScaleFactorCalculator.soEachBoundingBoxFits(boundingBoxes, targetSize);
        this.resizer = resizer;
        this.background = backgroundCreator.apply(scaleFactor);

        this.sizeScaled = background.map(ScaleableBackground::sizeAfterAnyScaling);

        this.objectsScaled =
                Scaler.scaleObjects(
                        allObjects,
                        scaleFactor,
                        overlappingObjects,
                        Optional.of(ObjectMask::flattenZ),
                        Optional.of(this::clipToScaledSizeIfKnown));
        this.objectsIndexed =
                IntersectingObjects.create(
                        ObjectCollectionFactory.of(objectsScaled.asCollectionOrderNotPreserved()));
    }

    /**
     * Clips an {@link ObjectMask} to the scaled size if known.
     *
     * @param object the {@link ObjectMask} to clip.
     * @return the clipped {@link ObjectMask}.
     */
    private ObjectMask clipToScaledSizeIfKnown(ObjectMask object) {
        if (sizeScaled.isPresent()) {
            return object.clampTo(sizeScaled.get());
        } else {
            return object;
        }
    }

    /**
     * Flattens and scales a stack if it exists.
     *
     * <p>Any resolution information is also removed.
     *
     * @param stack stack to flatten and scale
     * @return the flattened and scaled stack
     */
    public Optional<Stack> scaleStack(Optional<Stack> stack) {
        try {
            return OptionalUtilities.map(stack, this::flattenScaleAndRemoveResolution);
        } catch (OperationFailedException e) {
            // In this context, this exception should only occur if different sized channels are
            // produced for a stack,
            //  which cannot happen
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * Reads the extent from a stack that has already been scaled if it exists, or derives an extent
     * from an object collection.
     *
     * @param objectsUnscaled objects that have yet to be scaled (and flattened).
     * @return an {@link Extent} that has been flattened and scaled.
     */
    public Extent extentFromStackOrObjects(ObjectCollection objectsUnscaled) {
        return sizeScaled.orElseGet(() -> deriveScaledExtentFromObjects(objectsUnscaled));
    }

    /**
     * Flattens and scales objects.
     *
     * @param objects unscaled objects.
     * @return a scaled {@link ObjectCollection}.
     * @throws OperationFailedException if a scaling cannot successfully complete.
     */
    public ObjectCollection scaleObjects(ObjectCollection objects) throws OperationFailedException {
        try {
            return objects.stream().map(objectsScaled::scaledObjectFor);
        } catch (GetOperationFailedException e) {
            throw e.asOperationFailedException();
        }
    }

    /**
     * Objects (scaled) that intersect with a particular bounding-box.
     *
     * @param box a search occurs for objects that intersect with this box (which has already been
     *     scaled).
     * @param excludeFromAdding these objects are excluded from the search (specifically, any object
     *     found that has the same bounding-box and number of pixels).
     * @return the {@link ObjectCollection} that intersect with the bounding-box except any in
     *     {@code excludeFromAdding}.
     */
    public ObjectCollection objectsThatIntersectWith(
            BoundingBox box, ObjectCollection excludeFromAdding) {

        Set<ObjectMask> intersectingObjects = objectsIndexed.intersectsWith(box);

        Set<ObjectMask> excludeSet = excludeFromAdding.stream().toSet();

        // Filter away any objects in the exclude set
        return new ObjectCollection(
                intersectingObjects.stream().filter(item -> !excludeSet.contains(item)));
    }

    /**
     * Scales each channel in the stack by the scale-factor and removes any resolution information
     * (which is no longer physically valid).
     *
     * @param stack the {@link Stack} to scale and flatten.
     * @return the scaled and flattened {@link Stack}.
     * @throws OperationFailedException if the operation fails.
     */
    private Stack flattenScaleAndRemoveResolution(Stack stack) throws OperationFailedException {
        return stack.mapChannel(this::flattenScaleAndRemoveResolutionFromChannel);
    }

    /**
     * Scales the channel by the scale-factor and removes any resolution information (which is no
     * longer physically valid).
     *
     * @param channel the {@link Channel} to scale and flatten.
     * @return the scaled and flattened {@link Channel}.
     */
    private Channel flattenScaleAndRemoveResolutionFromChannel(Channel channel) {
        Channel scaled = channel.projectMax().scaleXY(scaleFactor, resizer);
        scaled.assignResolution(Optional.empty());
        return scaled;
    }

    /**
     * Derives what a scaled version of an extent would look like that fits all objects.
     *
     * @param objects the {@link ObjectCollection} to derive the extent from.
     * @return the derived {@link Extent}.
     */
    private Extent deriveScaledExtentFromObjects(ObjectCollection objects) {
        return ExtentToFitBoundingBoxes.derive(
                objects.streamStandardJava()
                        .map(object -> object.boundingBox().scale(scaleFactor).flattenZ()));
    }
}
