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
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;

/**
 * A file-path-resolver that adds additional methods that perform the same function but output a relative-path rather than an absolute path after fully resolving paths
 * 
 * <p>This is useful for combining with the MultiRootedFilePathPrefixer. This results in relative-paths keeping the same root, as when passed in</p>
 * 
 * @author Owen Feehan
 *
 */
public abstract class FilePathPrefixerAvoidResolve extends FilePathPrefixer {
	
	// START BEAN PROPERTIES
	/** 
	 * A directory in which to output the experiment-directory and files
	 * 
	 *  <p>If empty, first the bean will try to use any output-dir set in the input context if it exists, or otherwise use the system temp dir</p>
	 * */
	@BeanField @AllowEmpty
	private String outPathPrefix = "";
	// END BEAN PROPERTIES
	
	// Caches the calculation
	private Path resolvedRoot = null;
		
	public FilePathPrefixerAvoidResolve() {
		
	}
		
	public FilePathPrefixerAvoidResolve(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	
	@Override
	public FilePathPrefix rootFolderPrefix(String expName, FilePathPrefixerParams context) throws AnchorIOException {
		return new FilePathPrefix( resolveExperimentAbsoluteRootOut(expName, context) );
	}
	
	@Override
	public FilePathPrefix outFilePrefix(PathWithDescription input, String expName, FilePathPrefixerParams context)
			throws AnchorIOException {

		Path root = resolveExperimentAbsoluteRootOut(expName, context);
		return outFilePrefixFromPath(input, root);
	}

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
		String folder = new String( getOutPathPrefix() + File.separator + experimentIdentifier + File.separator );
		return new FilePathPrefix( Paths.get(folder) );
	}
	
	/**
	 * Provides a prefix which can be prepended to all output files. It avoids resolving relative-paths.
	 * 
	 * @param pathIn an input-path to match against
	 * @param experimentIdentifier an identifier for the experiment
	 * @return a prefixer
	 * @throws AnchorIOException TODO
	 */
	public FilePathPrefix outFilePrefixAvoidResolve( PathWithDescription input, String experimentIdentifier ) throws AnchorIOException {
		return outFilePrefixFromPath(
			input,
			rootFolderPrefixAvoidResolve(experimentIdentifier).getFolderPath()
		);
	}

	/** 
	 * Determines the out-file prefix from a path
	 * 
	 * @param path path to calculate prefix from
	 * @param descriptiveName descriptive-name of input
	 * @param root root of prefix
	 * @return folder/filename for prefixing
	 * @throws AnchorIOException
	 */
	protected abstract FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root ) throws AnchorIOException;
	
	/** The root of the experiment for outputting files */
	private Path resolveExperimentAbsoluteRootOut( String expName, FilePathPrefixerParams context ) {
		
		if (resolvedRoot==null) {
			resolvedRoot = selectResolvedPath(context).resolve(expName);
		}
		return resolvedRoot;
	}
	
	private Path selectResolvedPath(FilePathPrefixerParams context) {
		
		if (outPathPrefix.isEmpty()) {
			// If there's an outPathPrefix specified, then use it
			if (context.getOutputDirectory()!=null) {
				assert( context.getOutputDirectory().isAbsolute() );
				return context.getOutputDirectory();
			} else {
				// Otherwise use the system temporary directory
				return tempDir();
			}
		}
		
		return resolvePath(outPathPrefix);
	}
	
	private static Path tempDir() {
		String tempDir = System.getProperty("java.io.tmpdir");
		return Paths.get(tempDir);
	}

	public String getOutPathPrefix() {
		return outPathPrefix;
	}

	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
}
