/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer;

import org.anchoranalysis.core.text.LanguageUtilities;

/**
 * A count of the number of inputs. Thread-safe.
 *
 * @author Owen Feehan
 * @param <T>
 */
public class SummarizerCount<T> extends Summarizer<T> {

    private int count = 0;

    @Override
    public synchronized void add(T element) {
        count++;
    }

    // Describes all the extensions found
    @Override
    public synchronized String describe() {
        return String.format("Found %s.", LanguageUtilities.prefixPluralizeMaybe(count, "input"));
    }
}
