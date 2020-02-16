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
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveNameFromFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import com.owenfeehan.pathpatternfinder.PathPatternFinder;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;

/**
 * Finds a pattern in the descriptive name, and uses the region
 * from the first variable to the last-variable as the
 * descriptive-name
 * 
 * @author owen
 */
public class PatternSpan extends DescriptiveNameFromFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<DescriptiveFile> descriptiveNamesFor(Collection<File> files, String elseName) {
	
		// Convert to list
		List<Path> paths = listConvertToPath(files);
		
		if (paths.size()<=1) {
			// Everything's a constant, so there must only be a single file. Return the file-name.
			return listExtractFileName(files);
		}
		
		// For now, hard-coded case-insensitivity here, and in ExtractVariableSpan
		// TODO consider making it optional in both places
		Pattern pattern = PathPatternFinder.findPatternPath(paths, IOCase.INSENSITIVE );
		
		assert hasAtLeastOneVariableElement(pattern);
		
		return listExtractVariableSpan(files, pattern, elseName);
	}
	
	private List<DescriptiveFile> listExtractVariableSpan(Collection<File> files, Pattern pattern, String elseName) {
		return files.stream()
			.map( file ->
				new DescriptiveFile(
					file,
					ExtractVariableSpan.extractVariableSpan(file, pattern, elseName)
				)
			)
			.collect( Collectors.toList() );
	}
		
	private static List<DescriptiveFile> listExtractFileName(Collection<File> files) {
		return files.stream()
			.map( file -> new DescriptiveFile(file, extensionlessNameFromFile(file)) )
			.collect( Collectors.toList() );
	}
	
	// Convert all Files to Path
	private static List<Path> listConvertToPath( Collection<File> files ) {
		 return files.stream().map( file -> file.toPath() ).collect( Collectors.toList() );
	}
	
	// The file-name without an extension
	private static String extensionlessNameFromFile( File file ) {
		return FilenameUtils.removeExtension( file.getName() );
	}
	
	
	private static boolean hasAtLeastOneVariableElement( Pattern pattern ) {
		for( PatternElement e : pattern ) {
			if (!e.hasConstantValue()) {
				return true;
			}
		}
		return false;
	}
}
