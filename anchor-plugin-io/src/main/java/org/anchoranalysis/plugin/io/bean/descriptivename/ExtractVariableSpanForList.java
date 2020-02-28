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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.core.file.PathUtilities;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;

class ExtractVariableSpanForList {

	/**
	 * Extract Descriptive-Files from the files via the extracted pattern
	 * 
	 * <p> If any of descriptive-names are blank, then add on a bit of the constant portion to all names to make it non-blank</p>
	 * @param files files
	 * @param pattern extracted-pattern
	 * @param elseName fallback if it's still emtpy after operation
	 * @return
	 */
	public static List<DescriptiveFile> listExtract(Collection<File> files, Pattern pattern, String elseName) {
		List<DescriptiveFile> listMaybeEmpty = listExtractMaybeEmpty(files, pattern, elseName);
		
		if (hasAnyEmptyDescriptiveName(listMaybeEmpty)) {
			String prependStr = extractPrependString(pattern, elseName);
			return listPrepend(prependStr, listMaybeEmpty);
		} else {
			return listMaybeEmpty;
		}
	}
	
	private static List<DescriptiveFile> listPrepend( String prependStr, List<DescriptiveFile> list ) {
		return list.stream().map( df->
			new DescriptiveFile(
				df.getFile(),
				PathUtilities.toStringUnixStyle( prependStr + df.getDescriptiveName() ).trim()
			)
		).collect(Collectors.toList());
	}
	
	private static String extractPrependString( Pattern pattern, String elseName ) {
		// Find all the constant portions before the empty string
		// Replace any constants from the left-hand-side with empty strings
		
		String lastConstantElement = elseName;
		
		for( PatternElement element : pattern) {
			
			if (element.hasConstantValue()) {
				lastConstantElement = element.describe(10000000);
			} else {
				break;
			}
		}

		return extractLastComponent(lastConstantElement);
	}
	
	private static String extractLastComponent( String str ) {
		
		str = PathUtilities.toStringUnixStyle(str);
		
		if (str.length()==1) {
			return str;
		}
		
		// Find the index of the last forward-slash (or second-last if the final character is a forward-slash)
		String strMinusOne = str.substring( 0, str.length()-1 );
		int finalIndex = strMinusOne.lastIndexOf("/");
		return str.substring(finalIndex+1);
	}
	
	private static boolean hasAnyEmptyDescriptiveName( List<DescriptiveFile> list ) {
		return list.stream().filter( df-> df.getDescriptiveName().isEmpty() ).count()>0;
	}
	
	private static List<DescriptiveFile> listExtractMaybeEmpty(Collection<File> files, Pattern pattern, String elseName) {
		return files.stream()
			.map( file ->
				new DescriptiveFile(
					file,
					ExtractVariableSpan.extractVariableSpan(file, pattern, elseName)
				)
			)
			.collect( Collectors.toList() );
	}
	
}
