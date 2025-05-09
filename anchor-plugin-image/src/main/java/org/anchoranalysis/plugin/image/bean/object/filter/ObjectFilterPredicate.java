/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.object.filter;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Uses a predicate to make a decision whether to keep objects or not.
 *
 * @author Owen Feehan
 */
public abstract class ObjectFilterPredicate extends ObjectFilter {

    @Override
    public ObjectCollection filter(
            ObjectCollection objectsToFilter, Optional<Dimensions> dimensions)
            throws OperationFailedException {

        if (!precondition(objectsToFilter)) {
            return objectsToFilter;
        }

        start(dimensions, objectsToFilter);

        ObjectCollection filtered =
                objectsToFilter.stream()
                        .filter(
                                object -> match(object, dimensions),
                                OperationFailedException.class);

        end();

        return filtered;
    }

    /**
     * A precondition, which if evaluates to false, cancels the filter i.e. nothing is removed.
     *
     * @param objectsToFilter the {@link ObjectCollection} to be filtered
     * @return true if the filter should be applied, false otherwise
     */
    protected abstract boolean precondition(ObjectCollection objectsToFilter);

    /**
     * Performs any necessary initialization before filtering begins.
     *
     * @param dimensions optional {@link Dimensions} of the objects
     * @param objectsToFilter the {@link ObjectCollection} to be filtered
     * @throws OperationFailedException if the initialization fails
     */
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        // Default implementation, nothing to do
    }

    /**
     * A predicate condition for an object to be kept in the collection.
     *
     * @param object the {@link ObjectMask} to be evaluated
     * @param dimensions optional {@link Dimensions} of the object
     * @return true if the object should be kept, false otherwise
     * @throws OperationFailedException if the matching operation fails
     */
    protected abstract boolean match(ObjectMask object, Optional<Dimensions> dimensions)
            throws OperationFailedException;

    /**
     * Performs any necessary cleanup after filtering is complete.
     *
     * @throws OperationFailedException if the cleanup operation fails
     */
    protected abstract void end() throws OperationFailedException;
}
