/* (C)2020 */
package org.anchoranalysis.plugin.image.task.grouped;

import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Adds an individual-item to an aggregate-item
 *
 * @author Owen Feehan
 * @param <S> individual-item type
 * @param <T> aggregate-item type (comines many individual types)
 */
@FunctionalInterface
public interface AddToAggregateItem<S, T> {

    void add(S ind, T agg) throws OperationFailedException;
}
