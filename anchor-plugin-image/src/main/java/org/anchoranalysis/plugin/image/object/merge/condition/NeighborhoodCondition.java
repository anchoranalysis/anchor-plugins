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
 * A condition placed to determine if two objects could be potential neighbors are not (i.e.
 * potential candidates for merging)
 *
 * @author Owen Feehan
 */
public class NeighborhoodCondition implements UpdatableBeforeCondition {

    private boolean requireBBoxNeighbors;
    private boolean requireTouching;

    // START TEMPORARY objects, updated after every call to updateSrcObj
    // The bounding box of omSrcWithFeature grown by 1 in all directions (used for testing box
    // intersection)
    private BoundingBox boxSrcGrown;

    // The object-mask of omSrcWithFeature dilated by 1 in all directions (used for testing if
    // objects touch)
    private ObjectMask objectGrown;
    // END TEMPORARY objects

    public NeighborhoodCondition(boolean requireBBoxNeighbors, boolean requireTouching) {
        super();

        this.requireBBoxNeighbors = requireBBoxNeighbors;
        this.requireTouching = requireTouching;

        if (requireTouching) {
            this.requireBBoxNeighbors =
                    false; // We don't need this check, if we're testing actual object intersection
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

    private static BoundingBox boundingBoxGrown(ObjectMask object) {
        return GrowUtilities.grow(object.boundingBox());
    }
}
