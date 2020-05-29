package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.anchoranalysis.io.error.AnchorIOException;

/**
 * Removes any whitespace characters from the path
 * 
 * @author FEEHANO
 *
 */
public class RemoveWhitespace extends CopyFilesNamingOne {

	@Override
	public Optional<Path> destinationPathRelative(Path pathDelegate) throws AnchorIOException {
		String pathMinusWhiteSpace = NamingUtilities.convertToString(pathDelegate).replaceAll("\\s","");
		
		return Optional.of(
			Paths.get(pathMinusWhiteSpace)
		);
	}
}
