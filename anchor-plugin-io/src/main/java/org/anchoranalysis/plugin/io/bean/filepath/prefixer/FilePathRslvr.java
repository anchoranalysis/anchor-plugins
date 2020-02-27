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
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.apache.commons.io.FilenameUtils;

/// Matches prefixes against our incoming files, and attaches them to outgoing files 
public class FilePathRslvr extends FilePathPrefixerAvoidResolve {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2568294173350955724L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean includeFolders = true;
	// END BEAN PROPERTIES

	@Override
	protected FilePathPrefix outFilePrefixFromPath(Path path, Path root) throws AnchorIOException {
		// We strip the incoming path of it's extension
		Path pathInRemoved = Paths.get( FilenameUtils.removeExtension( path.toString() ) );
		
		FilePathDifferenceFromFolderPath ff = difference(pathInRemoved);
				
		Path outFolderPath = root;
		
		if (includeFolders && ff.getFolder()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFolder() );
		}
		
		if (isFileAsFolder() && ff.getFilename()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFilename() );
		}
		
		FilePathPrefix fpp = new FilePathPrefix( outFolderPath );
		
		if (!isFileAsFolder()) {
			fpp.setFilenamePrefix( ff.getFilename() + "_");
		}

		return fpp;	
	}
	
	private FilePathDifferenceFromFolderPath difference(Path pathInRemoved) throws AnchorIOException {
		FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
		ff.init(
			getInPathPrefixAsPath(),
			pathInRemoved
		);
		return ff;
	}

	public boolean isIncludeFolders() {
		return includeFolders;
	}

	public void setIncludeFolders(boolean includeFolders) {
		this.includeFolders = includeFolders;
	}
}