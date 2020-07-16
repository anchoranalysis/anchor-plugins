/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Summarizes a set of elements by outputting a descriptive string
 *
 * @author Owen Feehan
 * @param <T> type of element to be summarized
 */
public abstract class Summarizer<T> extends AnchorBean<Summarizer<T>> {

    /** Adds a element to the summary. Called one-time for each element */
    public abstract void add(T element) throws OperationFailedException;

    /**
     * Returns a string summarizing this item
     *
     * @return
     */
    public abstract String describe() throws OperationFailedException;
}
