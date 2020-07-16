/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer;

import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Creates a string where each line is an element (the string representation thereof)
 *
 * @author Owen Feehan
 * @param <T> type of entity to summarize (its string representation is taken)
 */
public class SummarizerListMultiline<T> extends Summarizer<T> {

    private StringBuilder sb = null;

    @Override
    public synchronized void add(T element) throws OperationFailedException {

        if (sb == null) {
            // First-line
            sb = new StringBuilder();
        } else {
            // Subsequent lines
            sb.append(System.lineSeparator());
        }

        sb.append(element.toString());
    }

    @Override
    public synchronized String describe() throws OperationFailedException {

        if (sb != null) {
            return sb.toString();
        } else {
            return "";
        }
    }
}
