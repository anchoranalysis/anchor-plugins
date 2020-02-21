package org.anchoranalysis.plugin.io.bean.descriptivename;

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
import org.apache.commons.io.IOCase;

import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;

/**
 * Extracts the variable-part of the pattern from the string
 * i.e. it ignores constant-pattern-elements on the left and right
 * 
 * @author owen
 *
 */
class ExtractVariableSpan {

	private static Logger log = Logger.getLogger(ExtractVariableSpan.class.getName());
	
	public static String extractVariableSpan(File file, Pattern pattern, String elseName) {
		try {
			return extractVariableSpanWithError(file, pattern);
		} catch (OperationFailedException e) {
			log.log(Level.WARNING, "Cannot extract a variable", e);
			return elseName;
		}
	}
		
	private static String extractVariableSpanWithError(File file, Pattern pattern) throws OperationFailedException {
		String path = file.toPath().toString();
		return trimConstantElementsFromBothSides( path, pattern, IOCase.INSENSITIVE );
	}
	
	private static String trimConstantElementsFromBothSides( String str, Pattern pattern, IOCase ioCase ) throws OperationFailedException {
		String[] fittedElements = pattern.fitAgainst(str, ioCase);
		
		if (fittedElements==null) {
			throw new OperationFailedException(
				String.format("Cannot match pattern %s against %s", pattern, str)
			);
		}
		
		assert( fittedElements.length ==pattern.size() );
		
		// Replace any constants from the left-hand-side with empty strings
		for( int i=0; i<fittedElements.length; i++) {
			if (pattern.get(i).hasConstantValue()) {
				fittedElements[i] = "";
			} else {
				break;
			}
		}
		
		// Replace any constants from the right-hand-side with empty strings
		for( int i=(fittedElements.length-1); i>=0; i--) {
			if (pattern.get(i).hasConstantValue()) {
				fittedElements[i] = "";
			} else {
				break;
			}
		}
		
		// Combine all strings to form the name
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<fittedElements.length; i++) {
			sb.append( fittedElements[i] );
		}
		return sb.toString();
	}
	
	
	private static String trimAllConstantElementsFromLeft( String strToTrim, Pattern pattern ) throws OperationFailedException {
		// Remove any constants from the left-most side
		for( PatternElement e : pattern ) {
			if (!e.hasConstantValue()) {
				break;
			}
			
			strToTrim = trimFromLeft(strToTrim, e);
		}
		
		return strToTrim;
	}
	
	private static String trimFromLeft( String strToTrim, PatternElement constantElement ) throws OperationFailedException {
		
		// For now, hard-coded case-insensitivity here, and in PatternSpan
		// TODO consider making it optional in both places
		String matchText = stringFromConstantPattern(constantElement).toLowerCase();
		
		int index = strToTrim.toLowerCase().indexOf( matchText );
		if(index!=0) {
			throw new OperationFailedException(
				String.format("Cannot match %s against %s from left-side", matchText, strToTrim)
			);
		}
		return strToTrim.substring( matchText.length() );
	}
	
	// As there's no nicer way via the API, we convert the constant-pattern to a string via describe()
	private static String stringFromConstantPattern( PatternElement e ) {
		return e.describe(1000000);
	}
}
