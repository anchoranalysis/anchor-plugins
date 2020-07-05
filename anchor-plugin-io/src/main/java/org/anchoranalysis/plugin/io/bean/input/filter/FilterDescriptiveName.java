package org.anchoranalysis.plugin.io.bean.input.filter;

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

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.input.filter.FilterDescriptiveNameEqualsContains;

/**
 * Filters all the input objects for only those with certain types of descriptive-names.
 * 
 * <p>Either or both <i>equals</i> or <i>contains</i> conditions are possible</p>
 * 
 * @author Owen Feehan
 *
 * @param <T> input-type
 */
public class FilterDescriptiveName<T extends InputFromManager> extends InputManager<T> {

	// START BEAN PROPERTIES
	@BeanField
	private InputManager<T> input;
	
	/** A descriptive-name must be exactly equal to (case-sensitive) this string. If empty, disabled. */
	@BeanField @AllowEmpty
	private String equals = "";
	
	/** A descriptive-name must contain (case-sensitive) this string. If empty, disabled. */
	@BeanField @AllowEmpty
	private String contains = "";
	// END BEAN PROPERTIES
	
	@Override
	public List<T> inputObjects(InputManagerParams params)
			throws AnchorIOException {
		
		FilterDescriptiveNameEqualsContains filter = new FilterDescriptiveNameEqualsContains(
			equals,
			contains	
		);
		
		return filter.removeNonMatching(
			input.inputObjects(params)		// Existing collection
		);
	}
	
	
	public InputManager<T> getInput() {
		return input;
	}

	public void setInput(InputManager<T> input) {
		this.input = input;
	}

	public String getEquals() {
		return equals;
	}

	public void setEquals(String equals) {
		this.equals = equals;
	}

	public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}

}
