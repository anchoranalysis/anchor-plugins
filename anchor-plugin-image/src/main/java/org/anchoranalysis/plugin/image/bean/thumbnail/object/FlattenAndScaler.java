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
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.cache.LRUCache;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.io.generator.raster.boundingbox.ScaleableBackground;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;

@RequiredArgsConstructor
class FlattenAndScaler {

    private static final int CACHE_SIZE = 1000;

    /** scale-factor to apply to objects and stacks */
    @Getter private final ScaleFactor scaleFactor;
    private final Interpolator interpolator;

    /**
     * As (when we are drawing outlines) we can end up scaling objects down multiple times, this
     * caches results for efficiency
     */
    private final LRUCache<ObjectMask, ObjectMask> cacheScaledObjects;
    
    /**
     * An efficiently searchable index of the unscaled objects, indexed by their scaled bounding-boxes
     */
    private ObjectCollectionRTree objectsIndexed;

    /**
     * Constructor 
     * 
     * @param boundingBoxes supplies a stream of bounding-boxes that specify each unscaled regions that we will generate thumbnails for
     * @param allObjects all the objects that can appear in the thumbnails
     * @param numberBoundingBoxes the total number of bounding-boxes in the stream
     * @param interpolator interpolator for scaling stack
     * @param targetSize the target size which objects will be scaled-down to fit inside
     */
    public FlattenAndScaler(StreamableCollection<BoundingBox> boundingBoxes, ObjectCollection allObjects, Interpolator interpolator, Extent targetSize) {
        this.scaleFactor = ScaleFactorCalculator.factorSoEachBoundingBoxFitsIn(boundingBoxes, targetSize);
        this.interpolator = interpolator;
        
        this.cacheScaledObjects =
                new LRUCache<>(
                        CACHE_SIZE, object -> object.flattenZ().scale(scaleFactor, interpolator));
        this.objectsIndexed = createRTree(allObjects);
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
     * Flattens and scales an object if it exists
     *
     * @param object unscaled object
     * @return a scaled object
     */
    public ObjectMask scaleObject(ObjectMask object) {
        try {
            return cacheScaledObjects.get(object);
        } catch (GetOperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * Flattens and scales an object if it exists
     *
     * @param object unscaled object
     * @return a scaled object
     * @throws OperationFailedException if one is thrown scaling an individual-object as per {@link #scaleObject}
     */
    public ObjectCollection scaleObjects(ObjectCollection objects) throws OperationFailedException {
        return objects.stream().map(this::scaleObject);
    }
    
    /**
     * Objects (scaled) that intersect with a particular bounding-box
     * 
     * @param box a search occurs for objects that intersect with this box
     * @param excludeFromAdding these objects are excluded from the search (specifically, any object found that has the same bounding-box and number of pixels)
     * @return 
     * @throws OperationFailedException if one is thrown scaling an individual-object as per {@link #scaleObject}
     */
    public ObjectCollection objectsThatIntersectWith(BoundingBox box, ObjectCollection excludeFromAdding) throws OperationFailedException {
        ObjectCollection objectsUnscaled = objectsIndexed.intersectsWith(box);

        ObjectCollection objectsScaled = scaleObjects(objectsUnscaled);
        
        // Remove any objects that intersect with what's already present by a serious degree
        // This isn't necessary the most efficient way of drawing the other objects
        // A better way would be to be aware of the indices of objects in the r-tree and exclude on that level
        // But for now, we do it this way, as it seems to get the job done.
        return objectsScaled.stream().filter( object -> ratioOverlapWith(object, excludeFromAdding)<0.1 );
    }
    
    private ObjectCollectionRTree createRTree(ObjectCollection allObjects) {
        return new ObjectCollectionRTree(
           allObjects,     
           object -> object.boundingBox().flattenZ().scale(scaleFactor)
        );
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
        Channel scaled = channel.maxIntensityProjection().scaleXY(scaleFactor, interpolator);
        scaled.updateResolution(new ImageResolution());
        return scaled;
    }

    /** Derives what a scaled version of an extent would look like that fits all objects */
    private Extent deriveScaledExtentFromObjects(ObjectCollection objects) {
        return ExtentToFitBoundingBoxes.derive(
                objects.streamStandardJava()
                        .map(object -> object.boundingBox().scale(scaleFactor).flattenZ()));
    }
        
    /**
     * A ratio expressing how much an object overlaps with another collection of objects.
     * <p>
     * Specifically, the number of overlapping voxels (between {@code object} and {@code overlapWith}) divided by the number of voxels in {@code object}
     * @param object object to measure overlap for
     * @param overlapWith objects that can potentially overlap with {@code object}
     * @return a ratio in the range [0, 1] where 0 is no overlap, and 1 is complete overlap.
     */
    private static double ratioOverlapWith( ObjectMask object, ObjectCollection overlapWith) {
        return ((double) overlapWith.countIntersectingVoxels(object))/object.numberVoxelsOn();
    }
}
