/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithContainer;

/**
 * Returns only the objects that intersect with at least one object in the container
 *
 * @author feehano
 */
public class IntersectsWithContainer extends ObjectCollectionProviderWithContainer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private boolean inverse = false; // If set, we return the objects that DO NOT intersect
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        ObjectCollection objectsContainer = containerRequired();

        return objects.stream().filter(object -> includeObject(object, objectsContainer));
    }

    private boolean includeObject(ObjectMask object, ObjectCollection objectsContainer) {
        boolean intersection = doesObjectIntersect(object, objectsContainer);

        if (inverse) {
            return !intersection;
        } else {
            return intersection;
        }
    }

    private static boolean doesObjectIntersect(ObjectMask object, ObjectCollection container) {
        return container.stream().anyMatch(object::hasIntersectingVoxels);
    }
}
