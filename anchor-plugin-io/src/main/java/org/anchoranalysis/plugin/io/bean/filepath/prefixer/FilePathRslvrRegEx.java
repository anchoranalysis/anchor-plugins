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
import org.anchoranalysis.core.file.PathUtilities;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

/// 

/**
 * Matches a regex against incoming file-paths to form a relative output
 * @author owen
 *
 */
public class FilePathRslvrRegEx extends FilePathPrefixerFileAsFolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2568294173350955724L;
	
	// START BEAN PROPERTIES
	@BeanField
	private RegEx regEx;
	// END BEAN PROPERTIES

	@Override
	protected FilePathPrefix outFilePrefixFromPath(Path path, String descriptiveName, Path root) throws AnchorIOException {

		String[] components = componentsFromPath(path,	root);
		
		return createPrefix(
			root,
			components,
			isFileAsFolder()
		);
	}
	
	
	private String[] componentsFromPath( Path pathIn, Path pathInPrefix ) throws AnchorIOException {
		
		Path matchIn = matchStringWithoutPrefix(pathIn, pathInPrefix );
		
		String matchInReplaced = PathUtilities.toStringUnixStyle( matchIn );
		
		String[] components = regEx.matchStr( matchInReplaced );
		
		if (components==null) {
			throw new AnchorIOException( String.format("Cannot match '%s'", matchInReplaced ));
		}
		
		return components;
	}
	
	private static Path matchStringWithoutPrefix( Path pathIn, Path inPathPrefix ) throws AnchorIOException {
		
		if (inPathPrefix!=null) {
			FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
			
			ff.initDirect(inPathPrefix, pathIn);
			return ff.getRemainderCombined();
		} else {
			return pathIn;
		}
	}
	
	/**
	 * Creates a FilePathPrefix based upon an ordered array of string components
	 * 
	 * @param outFolderPath the base folder of the prefixer
	 * @param components ordered array of string components
	 * @param fileAsFolder if TRUE, creates new sub-folders based upon matching groups. if FALSE, they are appended to the file-name instead
	 * @return
	 */
	private static FilePathPrefix createPrefix( Path outFolderPath, String[] components, boolean fileAsFolder ) {
		
		Path prefixPath = outFolderPath;
		
		if (fileAsFolder) {
			for (int g=0; g<components.length; g++) {
				prefixPath = prefixPath.resolve( components[g] );
			}
		}
		
		
		FilePathPrefix fpp = new FilePathPrefix( prefixPath );
		
		if (!fileAsFolder) {
			
			StringBuilder sb = new StringBuilder();
			for (int g=0; g<components.length; g++) {
				sb.append( components[g] );
				
				sb.append("_");
			}
			
			fpp.setFilenamePrefix( sb.toString() );
		}

		return fpp;
	}

	public RegEx getRegEx() {
		return regEx;
	}

	public void setRegEx(RegEx regEx) {
		this.regEx = regEx;
	}


}
