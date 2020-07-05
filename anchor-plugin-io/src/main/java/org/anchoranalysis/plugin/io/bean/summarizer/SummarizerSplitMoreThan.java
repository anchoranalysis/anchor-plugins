package org.anchoranalysis.plugin.io.bean.summarizer;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Multiplexes between two summarizers depending on the total number of count
 * 
 * <p>If the count is greater than a threshold, one summarizer is used, if not another</p>
 * @author Owen Feehan
 *
 * @param <T> type of element to be summarized
 */
public class SummarizerSplitMoreThan<T> extends Summarizer<T> {

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
