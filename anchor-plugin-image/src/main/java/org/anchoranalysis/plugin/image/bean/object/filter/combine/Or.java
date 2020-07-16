/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.combine;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Applies multiples filter with logical OR i.e. an object must pass any one of the filter steps to
 * remain.
 *
 * @author Owen Feehan
 */
public class Or extends ObjectFilterCombine {

    @Override
    public ObjectCollection filter(
            ObjectCollection objectsToFilter,
            Optional<ImageDimensions> dim,
            Optional<ObjectCollection> objectsRejected)
            throws OperationFailedException {

        // Stores any successful items in a set
        Set<ObjectMask> setAccepted = findAcceptedObjects(objectsToFilter, dim);

        // Adds the rejected-objects
        objectsRejected.ifPresent(
                rejected -> rejected.addAll(determineRejected(objectsToFilter, setAccepted)));

        // Creates the accepted-objects
        return ObjectCollectionFactory.fromSet(setAccepted);
    }

    /** Finds the accepted objects (i.e. objects that pass any one of the filters) */
    private Set<ObjectMask> findAcceptedObjects(
            ObjectCollection objectsToFilter, Optional<ImageDimensions> dim)
            throws OperationFailedException {
        Set<ObjectMask> setAccepted = new HashSet<>();

        for (ObjectFilter indFilter : getList()) {
            setAccepted.addAll(indFilter.filter(objectsToFilter, dim, Optional.empty()).asList());
        }

        return setAccepted;
    }

    /** Determines which objects are rejected */
    private static ObjectCollection determineRejected(
            ObjectCollection objectsToFilter, Set<ObjectMask> setAccepted) {
        return objectsToFilter.stream().filter(object -> !setAccepted.contains(object));
    }
}
