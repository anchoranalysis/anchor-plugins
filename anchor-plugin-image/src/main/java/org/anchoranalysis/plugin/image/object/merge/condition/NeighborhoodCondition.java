/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

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
    // The bounding box of omSrcWithFeature grown by 1 in all directions (used for testing bbox
    // intersection)
    private BoundingBox bboxSrcGrown;

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
    public void updateSourceObject(ObjectMask source, Optional<ImageResolution> res)
            throws OperationFailedException {

        bboxSrcGrown = requireBBoxNeighbors ? boundingBoxGrown(source) : null;

        try {
            if (requireTouching) {
                objectGrown =
                        MorphologicalDilation.createDilatedObject(
                                source, Optional.empty(), true, 1, false);
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
                && !bboxSrcGrown.intersection().existsWith(destination.getBoundingBox())) {
            return false;
        }

        return !requireTouching || objectGrown.hasIntersectingVoxels(destination);
    }

    private static BoundingBox boundingBoxGrown(ObjectMask object) {
        return GrowUtilities.grow(object.getBoundingBox());
    }
}
