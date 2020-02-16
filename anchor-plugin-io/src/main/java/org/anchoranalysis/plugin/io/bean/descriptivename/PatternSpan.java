package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveNameFromFile;
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
		
		Pattern pattern = PathPatternFinder.findPatternPath(paths, IOCase.INSENSITIVE );
		
		if (!hasAtLeastOneVariableElement(pattern)) {
			// Everything's a constant, so there must only be a single file. Return the file-name.
			return listExtractFileName(files);
		}
		
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
			.map( file -> new DescriptiveFile(file, file.getName()) )
			.collect( Collectors.toList() );
	}
	
	// Convert all Files to Path
	private static List<Path> listConvertToPath( Collection<File> files ) {
		 return files.stream().map( file -> file.toPath() ).collect( Collectors.toList() );
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
