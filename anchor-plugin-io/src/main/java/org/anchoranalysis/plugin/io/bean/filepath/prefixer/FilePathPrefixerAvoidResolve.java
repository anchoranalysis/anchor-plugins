package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.io.File;

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

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * A file-path-resolver that adds additional methods that preform the same function but output a relative-path rather than an absolute path after fully resolving paths
 * 
 * <p>This is useful for combining with the MultiRootedFilePathPrefixer. This results in relative-paths keeping the same root, as when passed in</p>
 * 
 * @author Owen Feehan
 *
 */
public abstract class FilePathPrefixerAvoidResolve extends FilePathPrefixerSpecifiedOutPath {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @AllowEmpty
	private String inPathPrefix = "";
	
	@BeanField
	private boolean fileAsFolder = true;
	// END BEAN PROPERTIES
	
	/**
	 * Provides a prefix that becomes the root-folder.  It avoids resolving relative-paths.
	 * 
	 * <p>This is an alternative method to rootFolderPrefix that avoids resolving the out-path prefix against the file system</p>
	 * 
	 * @param experimentIdentifier an identifier for the experiment
	 * @return a prefixer
	 * @throws IOException
	 */
	public FilePathPrefix rootFolderPrefixAvoidResolve( String experimentIdentifier )  {
		return createRootFolderPrefix( getOutPathPrefix(), experimentIdentifier );
	}
	
	/**
	 * Provides a prefix which can be prepended to all output files. It avoids resolving relative-paths.
	 * 
	 * @param pathIn an input-path to match against
	 * @param experimentIdentifier an identifier for the experiment
	 * @return a prefixer
	 * @throws AnchorIOException TODO
	 */
	public FilePathPrefix outFilePrefixAvoidResolve( Path pathIn, String experimentIdentifier ) throws AnchorIOException {
		return outFilePrefixFromPath(
			pathIn,
			rootFolderPrefixAvoidResolve(experimentIdentifier).getFolderPath()
		);
	}
	
	@Override
	protected FilePathPrefix outFilePrefixFromRoot( InputFromManager input, Path root ) throws AnchorIOException {
		return outFilePrefixFromPath(input.pathForBinding(), root);
	}
	
	protected abstract FilePathPrefix outFilePrefixFromPath( Path path, Path root ) throws AnchorIOException;
	
	public String getInPathPrefix() {
		return inPathPrefix;
	}
	
	public Path getInPathPrefixAsPath() {
		return Paths.get(inPathPrefix);
	}

	public void setInPathPrefix(String inPathPrefix) {
		this.inPathPrefix = inPathPrefix;
	}

	public boolean isFileAsFolder() {
		return fileAsFolder;
	}

	public void setFileAsFolder(boolean fileAsFolder) {
		this.fileAsFolder = fileAsFolder;
	}
	
	
	private static FilePathPrefix createRootFolderPrefix( String path, String experimentIdentifier ) {
		String folder = new String( path + File.separator + experimentIdentifier + File.separator );
		return new FilePathPrefix( Paths.get(folder) );
	}
	
}
