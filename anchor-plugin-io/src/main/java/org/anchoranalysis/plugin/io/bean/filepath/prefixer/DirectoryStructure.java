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

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.apache.commons.io.FilenameUtils;

/**
 * Reuses the directories between a path and its root to form the output - and also the filename.
 * 
 *  <p>e.g. for a path=<pre>/a/b/c/d/e.tif</pre> and root=<pre>/a/b</pre> then the prefix would be <pre>c/d/e/</p>
 *  
 * @author owen
 *
 */
public class DirectoryStructure extends FilePathPrefixerAvoidResolve {

	// START BEAN PROPERTIES
	/** If false, the folders are ignored, and only the file-name is used in the output */
	@BeanField
	private boolean includeFolders = true;
	
	@BeanField @AllowEmpty
	private String inPathPrefix = "";
	// END BEAN PROPERTIES

	@Override
	protected FilePathPrefix outFilePrefixFromPath(Path path, String descriptiveName, Path root) throws AnchorIOException {
		// We strip the incoming path of it's extension
		Path pathInRemoved = Paths.get( FilenameUtils.removeExtension( path.toString() ) );
		
		FilePathDifferenceFromFolderPath ff = difference(pathInRemoved);
				
		Path outFolderPath = root;
		
		if (includeFolders && ff.getFolder()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFolder() );
		}
		
		if (ff.getFilename()!=null) {
			outFolderPath = outFolderPath.resolve( ff.getFilename() );
		}
		
		return new FilePathPrefix( outFolderPath );
	}
	
	private FilePathDifferenceFromFolderPath difference(Path pathInRemoved) throws AnchorIOException {
		FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
		ff.init(
			Paths.get(inPathPrefix),
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
	
	
	public String getInPathPrefix() {
		return inPathPrefix;
	}

	public void setInPathPrefix(String inPathPrefix) {
		this.inPathPrefix = inPathPrefix;
	}
}