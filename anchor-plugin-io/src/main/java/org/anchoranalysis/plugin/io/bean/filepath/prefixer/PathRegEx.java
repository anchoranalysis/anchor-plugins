package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

/*
 * #%L
 * anchor-io
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

/// 

/**
 * Matches a regex against incoming file-paths to form a prefix for output
 * 
 * <p>Groups (i.e. parentheses) in the regular expression each form a directory in the output</p>
 * 
 * @author owen
 *
 */
public class PathRegEx extends FilePathPrefixerAvoidResolve {

	// START BEAN PROPERTIES
	/** Regular expression to use to match path with at least one group in it. */
	@BeanField
	private RegEx regEx;
	// END BEAN PROPERTIES

	@Override
	protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root) throws AnchorIOException {

		String[] components = componentsFromPath( input.getPath() );
		
		return createPrefix( root, components );
	}
	
	
	private String[] componentsFromPath( Path pathIn ) throws AnchorIOException {
		
		String pathInForwardSlashes = FilePathToUnixStyleConverter.toStringUnixStyle( pathIn );
		
		return regEx.match( pathInForwardSlashes ).orElseThrow( ()->
			new AnchorIOException(
				String.format("Cannot match '%s'", pathInForwardSlashes )
			)
		);
	}
	
	/**
	 * Creates a FilePathPrefix based upon an ordered array of string components
	 * 
	 * @param outFolderPath the base folder of the prefixer
	 * @param components ordered array of string components
	 * @return the file-path-prefix
	 */
	private static FilePathPrefix createPrefix( Path outFolderPath, String[] components ) {
		
		Path path = outFolderPath;

		for (int g=0; g<components.length; g++) {
			path = path.resolve( components[g] );
		}
		
		return new FilePathPrefix( path );
	}

	public RegEx getRegEx() {
		return regEx;
	}

	public void setRegEx(RegEx regEx) {
		this.regEx = regEx;
	}


}
