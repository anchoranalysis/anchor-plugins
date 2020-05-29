package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;

/**
 * Rejects files that fail to match a particular regular-expression
 * 
 * @author FEEHANO
 *
 */
public class EnsureRegExMatch extends CopyFilesNamingOneRegEx {

	// START BEAN PROPERTIES
	/** Iff TRUE, then a file is rejected if regEx matches and vice-versa (opposite to normal behaviour) */
	@BeanField
	private boolean invert = false;
	// END BEAN PROPERTIES
	
	@Override
	protected Optional<Path> destinationPathRelative(Path pathDelegate, String regex) throws AnchorIOException {
		if (acceptPath(pathDelegate, regex)) {
			return Optional.of(
				pathDelegate
			);
		} else {
			return Optional.empty();
		}
	}
	
	private boolean acceptPath(Path path, String regex) {
		boolean matches = NamingUtilities.convertToString(path).matches(regex);
		
		if (invert) {
			return !matches;
		} else {
			return matches;
		}
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
}
