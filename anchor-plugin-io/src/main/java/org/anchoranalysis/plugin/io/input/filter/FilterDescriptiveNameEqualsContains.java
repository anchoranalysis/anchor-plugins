package org.anchoranalysis.plugin.io.input.filter;

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

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.anchoranalysis.io.input.InputFromManager;

/**
 * Filters a list of input-objects by the descriptive name
 * 
 * @author owen
 *
 */
public class FilterDescriptiveNameEqualsContains {
	
	private String equals;
	private String contains;
	
	/**
	 * Constructor
	 * 
	 * @param equals if non-empty, any item that doesn't match this string is filtered away
	 * @param contains if non-empty, any item that doesn't contain this string is filtered away
	 */
	public FilterDescriptiveNameEqualsContains(String equals, String contains) {
		super();
		this.equals = equals;
		this.contains = contains;
	}
	
	public <T extends InputFromManager> List<T> removeNonMatching( List<T> in ) {
		
		// If no condition is applied, just pass pack the entire iterator
		if (!atLeastOneCondition()) {
			return in;
		}
		
		removeNonMatchingFrom(
			in,
			s -> combinedPredicate(s)
		);
		
		return in;
	}
	
	private static <T extends InputFromManager> void removeNonMatchingFrom( List<T> in, Predicate<String> predicate ) {
		
		Iterator<T> itr = in.listIterator();
		while(itr.hasNext()) {
			T item = itr.next();
			
			if (!predicate.test(item.descriptiveName())) {
				itr.remove();
			}
		}
	}
		
	private boolean atLeastOneCondition() {
		return !equals.isEmpty() || !contains.isEmpty();
	}
	
	private boolean combinedPredicate( String str ) {
		return nonEmptyAndPredicate(
			str,
			equals,
			(ref, test) -> ref.equals(test)
		) && nonEmptyAndPredicate(
			str,
			contains,
			(ref, test) -> test.contains(ref)				
		);
	}
	
	private static boolean nonEmptyAndPredicate( String strToTest, String strReference, BiFunction<String,String,Boolean> func ) {
		if (!strReference.isEmpty()) {
			return func.apply(strReference, strToTest);	
		} else {
			return true;
		}
	}	
	
}
