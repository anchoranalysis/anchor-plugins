package org.anchoranalysis.plugin.io.bean.filepath.rslvr;

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
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.apache.commons.io.FilenameUtils;

/// Matches prefixes against our incoming files, and attaches them to outgoing files 
public class FilePathRslvr extends FilePathRslvrBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2568294173350955724L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean includeFolders = true;
	// END BEAN PROPERTIES

	
	@Override
	public FilePathPrefix outFilePrefix(Path pathIn, String experimentIdentifier, boolean debugMode) throws IOException {
		return createPrefix(
			resolvePath( pathIn ),
			resolvePath( getInPathPrefixAsPath() ),
			rootFolderPrefix(experimentIdentifier, debugMode).getFolderPath(),
			includeFolders,
			isFileAsFolder()
		);
	}
	
	
	// create out file prefix
	@Override
	public FilePathPrefix outFilePrefixAvoidResolve( Path pathIn, String experimentIdentifier ) throws IOException {
		return createPrefix(
			pathIn,
			getInPathPrefixAsPath(),
			rootFolderPrefixAvoidResolve(experimentIdentifier).getFolderPath(),
			includeFolders,
			isFileAsFolder()
		);
	}

	private static FilePathPrefix createPrefix(
		Path pathIn,
		Path inPathPrefix,
		Path rootFolderPrefix,
		boolean includeFolders,
		boolean fileAsFolder
	) throws IOException {

		// We strip the incoming path of it's extension
		Path pathInRemoved = Paths.get( FilenameUtils.removeExtension( pathIn.toString() ) );
		
		FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
		ff.init(
			inPathPrefix,
			pathInRemoved
		);
				
		Path outFolderPath = rootFolderPrefix;
		
		if (includeFolders && ff.getFolder()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFolder() );
		}
		
		if (fileAsFolder && ff.getFilename()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFilename() );
		}
		
		FilePathPrefix fpp = new FilePathPrefix( outFolderPath );
		
		if (!fileAsFolder) {
			fpp.setFilenamePrefix( ff.getFilename() + "_");
		}

		return fpp;		
	}
	

	public boolean isIncludeFolders() {
		return includeFolders;
	}

	public void setIncludeFolders(boolean includeFolders) {
		this.includeFolders = includeFolders;
	}
}
