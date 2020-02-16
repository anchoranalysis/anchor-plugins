package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.file.PathUtilities;

import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import com.owenfeehan.pathpatternfinder.patternelements.StringUtilities;

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
		
		// Remove any constants from the left-most side
		path = trimAllConstantElementsFromLeft(path, pattern);
		
		// Reverse path and elements
		path = StringUtilities.reverse(path);
		pattern.reverse();
				
		try {
			// Remove any constants from the left-most side of the reserved string (i.e. what's really the right)
			path = trimAllConstantElementsFromLeft(path, pattern);
		} finally {
			// Unreverse the patterm
			pattern.reverse();
		}
		
		return PathUtilities.toStringUnixStyle(
			StringUtilities.reverse(path)
		);
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
		String matchText = stringFromConstantPattern(constantElement);
		int index = strToTrim.indexOf( matchText );
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
