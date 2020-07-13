package org.anchoranalysis.plugin.io.bean.task;

/*-
 * #%L
 * anchor-plugin-io
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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class TypedValueUtilities {

	public static List<String> createArrayFromList( List<String[]> list, int index ) throws OperationFailedException {
		
		List<String> out = new ArrayList<>();
		
		for( String[] s : list ) {
			out.add( removeQuotes(s[index]) );
		}
		return out;
	}
	
	public static List<TypedValue> createTypedArrayFromList( List<String[]> list, int index ) throws OperationFailedException {
		
		List<TypedValue> out = new ArrayList<>();
		
		for( String[] s : list ) {
			addGuessType( out, s[index] );
		}
		return out;
	}
	
	public static List<String> listFromArray( String[] arr ) throws OperationFailedException {
		
		List<String> out = new ArrayList<>();
		
		for( String s : arr ) {
			out.add( removeQuotes(s) );
		}
		return out;
	}
	
	public static List<TypedValue> typeArray( String[] arr ) throws OperationFailedException {
		
		List<TypedValue> out = new ArrayList<>();
		
		for( String s : arr ) {
			addGuessType( out, s );
		}
		return out;
	}
	
	private static String removeQuotes( String s ) throws OperationFailedException {
		if (s.charAt(0)!='\"') {
			throw new OperationFailedException("Quote character at start is missing");
		}
		if (s.charAt( s.length()-1)!='\"') {
			throw new OperationFailedException("Quote character at end is missing");
		}
		return s.substring(1, s.length()-1);
	}

	private static void addGuessType( List<TypedValue> out, String s ) throws OperationFailedException {
		if (s.charAt(0)=='\"') {
			out.add( new TypedValue( removeQuotes(s )) );
		} else {
			out.add( new TypedValue( s, true ) ); 
		}
	}
}
