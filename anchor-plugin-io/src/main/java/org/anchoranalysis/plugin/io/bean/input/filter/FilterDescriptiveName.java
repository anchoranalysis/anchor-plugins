package org.anchoranalysis.plugin.io.bean.input.filter;

/*-
 * #%L
 * anchor-io
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Filters all the input objects for only those with certain types of descriptive-names.
 * 
 * <p>Either or both <i>equals</i> or <i>contains</i> conditions are possible</p>
 * 
 * @author FEEHANO
 *
 * @param <T> input-manager type
 */
public class FilterDescriptiveName<T extends InputFromManager> extends InputManager<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

		// Existing collection 
		List<T> in = input.inputObjects(params);
		
		// If no condition is applied, just pass pack the entire iterator
		if (!atLeastOneCondition()) {
			return in;
		}
		
		applyFilter(in);
		
		return in;
	}
	
	private void applyFilter( List<T> in ) {
		
		Iterator<T> itr = in.listIterator();
		while(itr.hasNext()) {
			T item = itr.next();
			
			if (!predicate(item.descriptiveName())) {
				itr.remove();
			}
		}
	}
		
	private boolean atLeastOneCondition() {
		return !equals.isEmpty() || !contains.isEmpty();
	}
	
	private boolean predicate( String str ) {
		return nonEmptyAndPredicate(
			str,
			equals,
			(ref, test) -> ref.equals(test)
		) && nonEmptyAndPredicate(
			str,
			contains,
			(ref, test) -> ref.contains(test)				
		);
	}
	
	private static boolean nonEmptyAndPredicate( String strToTest, String strReference, BiFunction<String,String,Boolean> func ) {
		if (!strReference.isEmpty()) {
			return func.apply(strReference, strToTest);	
		} else {
			return true;
		}
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
