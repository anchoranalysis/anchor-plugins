/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolume;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 *
 * <p>Specifically, <code>relation(volume,threshold)</code> must be true.
 *
 * @author Owen Feehan
 */
public class ThresholdedVolume extends ObjectFilterRelation {

    // START BEAN PROPERTIES
    @BeanField private UnitValueVolume threshold;
    // END BEAN PROPERTIES

    private int thresholdResolved;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);
        thresholdResolved = resolveThreshold(dim);
    }

    @Override
    protected boolean match(
            ObjectMask object, Optional<ImageDimensions> dim, RelationToValue relation) {
        return relation.isRelationToValueTrue(object.numberVoxelsOn(), thresholdResolved);
    }

    private int resolveThreshold(Optional<ImageDimensions> dim) throws OperationFailedException {
        try {
            return (int) Math.floor(threshold.resolveToVoxels(dim.map(ImageDimensions::getRes)));
        } catch (UnitValueException e) {
            throw new OperationFailedException(e);
        }
    }
}
