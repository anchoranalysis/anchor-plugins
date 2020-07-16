/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Keeps objects which intersects with ANY ONE of a collection of other objects.
 *
 * @author Owen Feehan
 */
public class IntersectsWith extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    private ObjectCollection intersectWithAnyOneObject;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);
        try {
            intersectWithAnyOneObject = objects.create();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected boolean match(ObjectMask objectToIntersectWith, Optional<ImageDimensions> dim)
            throws OperationFailedException {
        return intersectWithAnyOneObject.stream()
                .anyMatch(objectMask -> objectMask.hasIntersectingVoxels(objectToIntersectWith));
    }

    @Override
    protected void end() throws OperationFailedException {
        super.end();
        intersectWithAnyOneObject = null;
    }
}
