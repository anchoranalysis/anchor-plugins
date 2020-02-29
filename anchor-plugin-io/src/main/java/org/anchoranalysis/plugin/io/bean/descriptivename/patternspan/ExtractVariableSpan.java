package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.file.PathUtilities;
import org.apache.commons.io.IOCase;

import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;

/**
 * Extracts the a "spanning part" of the pattern from the string.
 * 
 * <p>The spanning part should contain all variable elements, and optionally adjacent (or in between) constant elements</p>
 * 
 * @author owen
 *
 */
class ExtractVariableSpan {

	private static Logger log = Logger.getLogger(ExtractVariableSpan.class.getName());
	
	private Pattern pattern;
	private String elseName;
	private int indexSpanStart;
	private int indexSpanEnd;
		
	/**
	 * Constructor
	 * 
	 * @param pattern the pattern used to extract the spanning element
	 * @param elseName a constant used if failure occurs automatically extracting a descriptive-name
	 * @param indexSpanStart the index of the first element that should be included in the spanning-part
	 * @param indexSpanEnd the index of the last element that should be included in the spanning-part
	 */
	public ExtractVariableSpan(Pattern pattern, String elseName, int indexSpanStart, int indexSpanEnd) {
		super();
		this.pattern = pattern;
		this.elseName = elseName;
		this.indexSpanStart = indexSpanStart;
		this.indexSpanEnd = indexSpanEnd;
		assert( indexSpanEnd >= indexSpanStart );
	}

	/**
	 * The right-most constant element before the span portion begins
	 * @return a string describing this constant element
	 */
	public String extractConstantElementBeforeSpanPortion() {
		// Find all the constant portions before the empty string
		// Replace any constants from the left-hand-side with empty strings
		
		if (indexSpanStart==0) {
			return elseName;
		}

		PatternElement element = pattern.get( indexSpanStart-1 ); 
		
		assert( element.hasConstantValue() );
		
		return PatternUtilities.constantElementAsString(element);
	}
	
	/**
	 * Extracts the spanning-portion from a particular file, ensuring it has UNIX-style path
	 * 
	 * @param file
	 * @return
	 */
	public String extractSpanPortionFor(File file) {
		return PathUtilities.toStringUnixStyle(
			extractVariableSpanUnnormalized(file)
		);
	}
	
	private String extractVariableSpanUnnormalized(File file) {
		try {
			return extractVariableSpanWithError(file);
		} catch (OperationFailedException e) {
			log.log(Level.WARNING, "Cannot extract a variable", e);
			return elseName;
		}
	}
				
	private String extractVariableSpanWithError(File file) throws OperationFailedException {
		String path = file.toPath().toString();
		return trimConstantElementsFromBothSides( path, IOCase.INSENSITIVE );
	}
	
	private String trimConstantElementsFromBothSides( String str, IOCase ioCase ) throws OperationFailedException {
		String[] fittedElements = pattern.fitAgainst(str, ioCase);
		
		if (fittedElements==null) {
			throw new OperationFailedException(
				String.format("Cannot match pattern %s against %s", pattern, str)
			);
		}
		
		assert( fittedElements.length ==pattern.size() );
		
		// Replace any constants from the left-hand-side with empty strings
		for( int i=0; i<indexSpanStart; i++) {
			assert(pattern.get(i).hasConstantValue());
			fittedElements[i] = "";
		}
		
		// Replace any constants from the right-hand-side with empty strings
		for( int i=(fittedElements.length-1); i>indexSpanEnd; i--) {
			fittedElements[i] = "";
		}
		
		// Combine all strings to form the name
		return combineStr(fittedElements);
	}
	
	private static String combineStr( String[] arr ) {
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<arr.length; i++) {
			sb.append( arr[i] );
		}
		return sb.toString();		
	}
}