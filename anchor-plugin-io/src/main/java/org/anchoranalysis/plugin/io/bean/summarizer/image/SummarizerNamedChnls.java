/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.summarizer.FrequencyMap;

/**
 * Summarzes {@link NamedChnlsInput} in different ways
 *
 * @author Owen Feehan
 * @param <T> type used for summary in frequency-map
 */
public abstract class SummarizerNamedChnls<T> extends Summarizer<NamedChnlsInput> {

    private FrequencyMap<T> map = new FrequencyMap<>();

    @Override
    public String describe() throws OperationFailedException {
        return map.describe(describeNoun());
    }

    protected abstract String describeNoun();

    protected void incrCount(T key) {
        map.incrCount(key);
    }
}
