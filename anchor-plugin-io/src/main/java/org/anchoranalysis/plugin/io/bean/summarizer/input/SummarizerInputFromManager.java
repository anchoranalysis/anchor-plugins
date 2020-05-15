package org.anchoranalysis.plugin.io.bean.summarizer.input;

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
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;

/**
 * Extracts a particular item from an InputManager for summarization downstream
 * 
 * <p>Can be used to (effectively) convert summarization from an InputFromManager to another type</p>
 * 
 * @author owen
 *
 * @param <T> input-type for input-manager
 * @param <S> type of entity extracted from input-type for summarization
 */
public abstract class SummarizerInputFromManager<T extends InputFromManager,S> extends Summarizer<T> {

	// START BEAN PROPERTIES
	@BeanField
	private Summarizer<S> summarizer;
	// END BEAN PROPERTIES
	
	@Override
	public void add(T element) throws OperationFailedException {

		S extractedElement = extractFrom(element);
		summarizer.add( extractedElement );
	}

	@Override
	public String describe() throws OperationFailedException {
		return summarizer.describe();
	}
	
	protected abstract S extractFrom( T input );

	public Summarizer<S> getSummarizer() {
		return summarizer;
	}

	public void setSummarizer(Summarizer<S> summarizer) {
		this.summarizer = summarizer;
	}
}
