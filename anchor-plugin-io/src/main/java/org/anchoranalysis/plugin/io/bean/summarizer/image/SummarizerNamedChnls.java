package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.summarizer.FrequencyMap;


/**
 * Summarzes {@link NamedChnlsInputAsStack} in different ways
 * 
 * @author owen
 *
 * @param <T> type used for summary in frequency-map
 */
public abstract class SummarizerNamedChnls<T> extends Summarizer<NamedChnlsInputAsStack> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FrequencyMap<T> map = new FrequencyMap<>();
	
	@Override
	public String describe() throws OperationFailedException {
		return map.describe( describeNoun() );
	}
	
	protected abstract String describeNoun();

	protected void incrCount( T key ) {
		map.incrCount(key);
	}
}
