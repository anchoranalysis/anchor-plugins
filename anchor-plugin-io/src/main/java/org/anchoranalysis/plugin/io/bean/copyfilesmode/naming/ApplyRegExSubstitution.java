package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;

/**
 * A regular expression substitution (replaceAll) is applied to the relative-path
 *   
 * @author FEEHANO
 *
 */
public class ApplyRegExSubstitution extends CopyFilesNamingOneRegEx {

	// START BEAN PROPERTIES
	@BeanField
	private String replacement;
	// END BEAN PROPERTIES
	
	@Override
	protected Optional<Path> destinationPathRelative(Path pathDelegate, String regex) throws AnchorIOException {
		String pathAfterRegEx = NamingUtilities.convertToString(pathDelegate).replaceAll(regex, replacement);
		return Optional.of(
			Paths.get(pathAfterRegEx)
		);
	}
	
	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}
