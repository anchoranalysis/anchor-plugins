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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.extent.rtree.ObjectCollectionRTree;
import org.anchoranalysis.image.extent.scale.ScaleFactor;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.io.generator.raster.boundingbox.ScaleableBackground;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.image.object.scale.ScaledElements;
import org.anchoranalysis.image.object.scale.Scaler;
import org.anchoranalysis.image.stack.Stack;

@RequiredArgsConstructor
class FlattenAndScaler {

    /** scale-factor to apply to objects and stacks */
    @Getter private final ScaleFactor scaleFactor;

    private final Interpolator interpolator;

    /** A scaled version of the objects */
    private ScaledElements<ObjectMask> objectsScaled;

    /**
     * An efficiently searchable index of the unscaled objects, indexed by their scaled
     * bounding-boxes
     */
    private ObjectCollectionRTree objectsIndexed;

    /**
     * Constructor
     *
     * @param boundingBoxes supplies a stream of bounding-boxes that specify each unscaled regions
     *     that we will generate thumbnails for
     * @param allObjects all the objects that can appear in the thumbnails
     * @param interpolator interpolator for scaling stack
     * @param targetSize the target size which objects will be scaled-down to fit inside
     * @throws OperationFailedException if there are too many objects
     */
    public FlattenAndScaler(
            StreamableCollection<BoundingBox> boundingBoxes,
            ObjectCollection allObjects,
            Interpolator interpolator,
            Extent targetSize)
            throws OperationFailedException {
        this.scaleFactor =
                ScaleFactorCalculator.factorSoEachBoundingBoxFitsIn(boundingBoxes, targetSize);
        this.interpolator = interpolator;

        this.objectsScaled =
                Scaler.scaleObjects(allObjects, scaleFactor, Optional.of(ObjectMask::flattenZ), Optional.empty());
        this.objectsIndexed =
                new ObjectCollectionRTree(
                        ObjectCollectionFactory.of(objectsScaled.asCollectionOrderNotPreserved()));
    }

    /**
     * Flattens and scales a stack if it exists
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
     * from an object collection
     *
     * @param background the background-image
     * @param objectsUnscaled objects that have yet to be scaled (and flattened)
     * @return an extent that has been flattened and scaled
     */
    public Extent extentFromStackOrObjects(
            Optional<ScaleableBackground> background, ObjectCollection objectsUnscaled) {
        return background
                .map(ScaleableBackground::extentAfterAnyScaling)
                .orElseGet(() -> deriveScaledExtentFromObjects(objectsUnscaled));
    }

    /**
     * Flattens and scales objects
     *
     * @param objects unscaled objects
     * @return a scaled object
     * @throws OperationFailedException
     */
    public ObjectCollection scaleObjects(ObjectCollection objects) throws OperationFailedException {
        try {
            return objects.stream().map(objectsScaled::scaledObjectFor);
        } catch (GetOperationFailedException e) {
            throw e.asOperationFailedException();
        }
    }

    /**
     * Objects (scaled) that intersect with a particular bounding-box
     *
     * @param box a search occurs for objects that intersect with this box (which has already been
     *     scaled)
     * @param excludeFromAdding these objects are excluded from the search (specifically, any object
     *     found that has the same bounding-box and number of pixels)
     * @return the objects that intersect with the bounding-box except any in {@code
     *     excludeFromAdding}
     */
    public ObjectCollection objectsThatIntersectWith(
            BoundingBox box, ObjectCollection excludeFromAdding) {

        ObjectCollection intersectingObjects = objectsIndexed.intersectsWith(box);

        Set<ObjectMask> excludeSet = excludeFromAdding.stream().toSet();

        return intersectingObjects.stream().filterExclude(excludeSet::contains);
    }

    /**
     * Scales each channel in the stack by the scale-factor and removes any resolution information
     * (which is no longer physically valid)
     */
    private Stack flattenScaleAndRemoveResolution(Stack stack) throws OperationFailedException {
        return stack.mapChannel(this::flattenScaleAndRemoveResolutionFromChannel);
    }

    /**
     * Scales the channel by the scale-factor and removes any resolution information (which is no
     * longer physically valid)
     */
    private Channel flattenScaleAndRemoveResolutionFromChannel(Channel channel) {
        Channel scaled = channel.projectMax().scaleXY(scaleFactor, interpolator);
        scaled.updateResolution(Optional.empty());
        return scaled;
    }

    /** Derives what a scaled version of an extent would look like that fits all objects */
    private Extent deriveScaledExtentFromObjects(ObjectCollection objects) {
        return ExtentToFitBoundingBoxes.derive(
                objects.streamStandardJava()
                        .map(object -> object.boundingBox().scale(scaleFactor).flattenZ()));
    }
}
