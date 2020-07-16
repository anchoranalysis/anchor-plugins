/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Keeps only objects that are not adjacent to the scene-border (i.e. have a bounding-box on the
 * very edge of the image)
 *
 * @author Owen Feehan
 */
public class NotTouchingSceneBorder extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField private boolean includeZ = false;
    // END BEAN PROPERTIES

    @Override
    protected boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException {

        if (!dim.isPresent()) {
            throw new OperationFailedException("Image-dimensions are required for this operation");
        }

        if (object.getBoundingBox().atBorderXY(dim.get())) {
            return false;
        }

        if (includeZ) {
            ReadableTuple3i cornerMin = object.getBoundingBox().cornerMin();
            if (cornerMin.getZ() == 0) {
                return false;
            }

            ReadableTuple3i cornerMax = object.getBoundingBox().calcCornerMax();
            if (cornerMax.getZ() == (dim.get().getZ() - 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIncludeZ() {
        return includeZ;
    }

    public void setIncludeZ(boolean includeZ) {
        this.includeZ = includeZ;
    }
}
