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

package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalDilation;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * A condition to determine if two objects are potential neighbors and candidates for merging.
 *
 * <p>This condition implements {@link UpdatableBeforeCondition} to allow efficient updates when the source object changes.
 */
public class NeighborhoodCondition implements UpdatableBeforeCondition {

    /** Whether to require bounding boxes to be neighbors. */
    private boolean requireBBoxNeighbors;

    /** Whether to require objects to be touching. */
    private boolean requireTouching;

    /** The bounding box of the source object grown by 1 in all directions. */
    private BoundingBox boxSrcGrown;

    /** The object-mask of the source object dilated by 1 in all directions. */
    private ObjectMask objectGrown;

    /**
     * Creates a new {@link NeighborhoodCondition}.
     *
     * @param requireBBoxNeighbors whether to require bounding boxes to be neighbors
     * @param requireTouching whether to require objects to be touching
     */
    public NeighborhoodCondition(boolean requireBBoxNeighbors, boolean requireTouching) {
        this.requireBBoxNeighbors = requireBBoxNeighbors;
        this.requireTouching = requireTouching;

        if (requireTouching) {
            this.requireBBoxNeighbors = false; // We don't need this check if we're testing actual object intersection
        }
    }

    @Override
    public void updateSourceObject(ObjectMask source, Optional<UnitConverter> unitConverter)
            throws OperationFailedException {

        boxSrcGrown = requireBBoxNeighbors ? boundingBoxGrown(source) : null;

        try {
            if (requireTouching) {
                objectGrown =
                        MorphologicalDilation.dilate(source, Optional.empty(), true, 1, false);
            } else {
                objectGrown = null;
            }
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean accept(ObjectMask destination) {

        // If this is set, we ignore any combinations whose bounding boxes don't touch or intersect
        if (requireBBoxNeighbors
                && !boxSrcGrown.intersection().existsWith(destination.boundingBox())) {
            return false;
        }

        return !requireTouching || objectGrown.hasIntersectingVoxels(destination);
    }

    /**
     * Grows the bounding box of an {@link ObjectMask} by 1 in all directions.
     *
     * @param object the {@link ObjectMask} whose bounding box to grow
     * @return the grown {@link BoundingBox}
     */
    private static BoundingBox boundingBoxGrown(ObjectMask object) {
        return GrowUtilities.grow(object.boundingBox());
    }
}