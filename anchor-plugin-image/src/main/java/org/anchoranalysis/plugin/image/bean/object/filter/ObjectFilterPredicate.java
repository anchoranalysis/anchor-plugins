/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Uses a predicate to make a decision whether to keep objects or not.
 *
 * @author Owen Feehan
 */
public abstract class ObjectFilterPredicate extends ObjectFilter {

    @Override
    public ObjectCollection filter(
            ObjectCollection objectsToFilter,
            Optional<ImageDimensions> dim,
            Optional<ObjectCollection> objectsRejected)
            throws OperationFailedException {

        if (!precondition(objectsToFilter)) {
            return objectsToFilter;
        }

        start(dim, objectsToFilter);

        ObjectCollection dup =
                objectsToFilter.stream().filter(object -> match(object, dim), objectsRejected);

        end();

        return dup;
    }

    /** A precondition, which if evaluates to false, cancels the filter i.e. nothing is removed */
    protected boolean precondition(ObjectCollection objectsToFilter) {
        return true;
    }

    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        // Default implementation, nothing to do
    }

    /** A predicate condition for an object to be kept in the collection */
    protected abstract boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException;

    protected void end() throws OperationFailedException {
        // Default implementation, nothing to do
    }
}
