/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.input;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;

/**
 * Extracts a particular item from an InputManager for summarization downstream
 *
 * <p>Can be used to (effectively) convert summarization from an InputFromManager to another type
 *
 * @author Owen Feehan
 * @param <T> input-type for input-manager
 * @param <S> type of entity extracted from input-type for summarization
 */
public abstract class SummarizerInputFromManager<T extends InputFromManager, S>
        extends Summarizer<T> {

    // START BEAN PROPERTIES
    @BeanField private Summarizer<S> summarizer;
    // END BEAN PROPERTIES

    @Override
    public void add(T element) throws OperationFailedException {
        OptionalUtilities.ifPresent(extractFrom(element), e -> summarizer.add(e));
    }

    @Override
    public String describe() throws OperationFailedException {
        return summarizer.describe();
    }

    protected abstract Optional<S> extractFrom(T input);

    public Summarizer<S> getSummarizer() {
        return summarizer;
    }

    public void setSummarizer(Summarizer<S> summarizer) {
        this.summarizer = summarizer;
    }
}
