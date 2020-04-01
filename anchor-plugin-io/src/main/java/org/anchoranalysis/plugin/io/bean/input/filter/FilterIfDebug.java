package org.anchoranalysis.plugin.io.bean.input.filter;

import java.util.Arrays;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.input.filter.FilterDescriptiveNameEqualsContains;

/**
 * Filters a list of inputs when in debug-mode
 * 
 * @author owen
 *
 * @param <T> input-type
 */
public class FilterIfDebug<T extends InputFromManager> extends InputManager<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private InputManager<T> input;
	// END BEAN PROPERTIES
	
	@Override
	public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

		List<T> unfiltered = input.inputObjects(params);
				
		if (params.isDebugModeActivated()) {
			return takeFirst(
				maybeFilteredList(unfiltered, params)
			);
		} else {
			return unfiltered;
		}
	}
	
	/** Take first item, if there's more than one in a list */
	private List<T> takeFirst( List<T> list ) {
		if (list.size()<=1) {
			// Nothing to do
			return list;
		} else {
			return Arrays.asList( list.get(0) );
		}
	}
	
	private List<T> maybeFilteredList( List<T> unfiltered, InputManagerParams params ) {
		FilterDescriptiveNameEqualsContains filter = new FilterDescriptiveNameEqualsContains(
			"",	// Always disabled
			params.getDebugModeParams().containsOrEmpty()	
		);
		
		return filter.removeNonMatching(unfiltered);
	}

	public InputManager<T> getInput() {
		return input;
	}

	public void setInput(InputManager<T> input) {
		this.input = input;
	}

}
