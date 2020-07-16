/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.combine;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Applies multiples filter with logical AND i.e. an object must pass all objects in the list to
 * remain.
 *
 * @author Owen Feehan
 */
public class And extends ObjectFilterCombine {

    @Override
    public ObjectCollection filter(
            ObjectCollection objects,
            Optional<ImageDimensions> dim,
            Optional<ObjectCollection> objectsRejected)
            throws OperationFailedException {

        ObjectCollection running = objects;

        for (ObjectFilter indFilter : getList()) {
            running = indFilter.filter(running, dim, objectsRejected);
        }

        return running;
    }
}
