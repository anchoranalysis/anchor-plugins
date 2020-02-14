package org.anchoranalysis.plugin.io.bean.summarizer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Multiplexes between two summarizers depending on the total number of count
 * 
 * <p>If the count is greater than a threshold, one summarizer is used, if not another</p>
 * @author owen
 *
 * @param <T> type of element to be summarized
 */
public class SummarizerSplitMoreThan<T> extends Summarizer<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** If there are more than <code>countThreshold</code> elements, use summarizerGreaterThan, else summarizerElse */
	@BeanField
	private int countThreshold;
	
	/** Used for summary if count(elements) > countThreshold */
	@BeanField
	private Summarizer<T> summarizerGreaterThan;
	
	/** Used for summary if count(elements) <= countThreshold */
	@BeanField
	private Summarizer<T> summarizerElse;
	// END BEAN PROPERTIES
	
	private int count = 0;
	
	@Override
	public void add(T element) throws OperationFailedException {
		count++;
		summarizerGreaterThan.add(element);
		summarizerElse.add(element);
	}

	@Override
	public String describe() throws OperationFailedException {
		Summarizer<T> summarizer = (count>countThreshold) ? summarizerGreaterThan : summarizerElse;
		return summarizer.describe();
	}
	
	public int getCountThreshold() {
		return countThreshold;
	}

	public void setCountThreshold(int countThreshold) {
		this.countThreshold = countThreshold;
	}

	public Summarizer<T> getSummarizerGreaterThan() {
		return summarizerGreaterThan;
	}

	public void setSummarizerGreaterThan(Summarizer<T> summarizerGreaterThan) {
		this.summarizerGreaterThan = summarizerGreaterThan;
	}

	public Summarizer<T> getSummarizerElse() {
		return summarizerElse;
	}

	public void setSummarizerElse(Summarizer<T> summarizerElse) {
		this.summarizerElse = summarizerElse;
	}
}
