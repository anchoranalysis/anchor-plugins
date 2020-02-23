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


import java.io.IOException;
import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.core.file.PathUtilities;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.anchoranalysis.io.input.InputFromManager;

/// 

/**
 * Matches a regex against incoming file-paths to form a relative output
 * @author owen
 *
 */
public class FilePathRslvrRegEx extends FilePathPrefixerAvoidResolve {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2568294173350955724L;
	
	// START BEAN PROPERTIES
	@BeanField
	private RegEx regEx;
	// END BEAN PROPERTIES

	@Override
	public FilePathPrefix outFilePrefix(InputFromManager input, String experimentIdentifier, FilePathPrefixerParams context) throws IOException {
		// we convert the input-path to absolute
		Path pathInAbsolute = resolvePath( input.pathForBinding() );
		Path inPathPrefixAbsolute = resolvePath( getInPathPrefixAsPath() );
		
		String[] components = componentsFromPath(
			pathInAbsolute,
			inPathPrefixAbsolute
		);
		
		return HelperFilePathRslvr.createPrefix(
			rootFolderPrefix(experimentIdentifier, context).getFolderPath(),
			components,
			isFileAsFolder()
		);
	}
	
	
	// create out file prefix
	@Override
	public FilePathPrefix outFilePrefixAvoidResolve( Path pathIn, String experimentIdentifier ) throws IOException {
		String[] components = componentsFromPath(
			pathIn,
			getInPathPrefixAsPath()
		);
		
		return HelperFilePathRslvr.createPrefix(
			rootFolderPrefixAvoidResolve(experimentIdentifier).getFolderPath(),
			components,
			isFileAsFolder()
		);
		
	
	}
	
	
	private String[] componentsFromPath( Path pathIn, Path pathInPrefix ) throws IOException {
		
		Path matchIn = matchStringWithoutPrefix(pathIn, pathInPrefix );
		
		String matchInReplaced = PathUtilities.toStringUnixStyle( matchIn );
		
		String[] components = regEx.matchStr( matchInReplaced );
		
		if (components==null) {
			throw new IOException( String.format("Cannot match '%s'", matchInReplaced ));
		}
		
		return components;
	}
	
	private static Path matchStringWithoutPrefix( Path pathIn, Path inPathPrefix ) throws IOException {
		
		if (inPathPrefix!=null) {
			FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
			
			ff.initDirect(inPathPrefix, pathIn);
			return ff.getRemainderCombined();
		} else {
			return pathIn;
		}
	}

	public RegEx getRegEx() {
		return regEx;
	}

	public void setRegEx(RegEx regEx) {
		this.regEx = regEx;
	}
}
