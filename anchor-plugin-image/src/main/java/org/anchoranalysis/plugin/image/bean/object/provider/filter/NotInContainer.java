/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithContainer;

/**
 * Filters objects to keep only those which are NOT in the container.
 *
 * @author Owen Feehan
 */
public class NotInContainer extends ObjectCollectionProviderWithContainer {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws CreateException {
        ObjectCollection container = containerRequired();
        return objectCollection.stream().filter(object -> !isObjectInContainer(object, container));
    }

    private static boolean isObjectInContainer(ObjectMask object, ObjectCollection container) {
        return container.stream().anyMatch(object::equals);
    }
}
