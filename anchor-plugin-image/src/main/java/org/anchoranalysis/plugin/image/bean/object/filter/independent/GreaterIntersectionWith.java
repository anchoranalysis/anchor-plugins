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
 * Only accepts an object if it has greater (or EQUAL) intersection with {@code objectsGreater} than
 * {@code objectsLesser}
 *
 * <p>If an object intersects with neither, it still gets accepted, as both return 0
 *
 * @author Owen Feehan
 */
public class GreaterIntersectionWith extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsGreater;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsLesser;
    // END BEAN PROPERTIES

    private ObjectCollection intersectionGreater;
    private ObjectCollection intersectionLesser;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);
        try {
            intersectionGreater = objectsGreater.create();
            intersectionLesser = objectsLesser.create();

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException {

        int cntGreater = intersectionGreater.countIntersectingVoxels(object);
        int cntLesser = intersectionLesser.countIntersectingVoxels(object);

        return cntGreater >= cntLesser;
    }

    @Override
    protected void end() throws OperationFailedException {
        super.end();
        intersectionGreater = null;
        intersectionLesser = null;
    }
}
