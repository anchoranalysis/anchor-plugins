package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.anchoranalysis.io.input.InputFromManager;

public abstract class FilePathPrefixerSpecifiedOutPath extends FilePathPrefixer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** 
	 * A directory in which to output the experiment-directory and files
	 * 
	 *  If empty, first the bean will try to use any output-dir set in the input context if it exists, or otherwise use the system temp dir
	 * */
	@BeanField @AllowEmpty
	private String outPathPrefix = "";
	// END BEAN PROPERTIES
	
	// Caches the calculation
	private Path resolvedRoot = null;
	
	public FilePathPrefixerSpecifiedOutPath() {
	}
	
	public FilePathPrefixerSpecifiedOutPath(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	
	@Override
	public FilePathPrefix rootFolderPrefix(String expName, FilePathPrefixerParams context) {
		return new FilePathPrefix( resolveExperimentAbsoluteRootOut(expName, context) );
	}
	
	@Override
	public FilePathPrefix outFilePrefix(InputFromManager input, String expName, FilePathPrefixerParams context)
			throws IOException {

		Path root = resolveExperimentAbsoluteRootOut(expName, context);
		return outFilePrefixFromRoot(input, root);
	}
	
	protected abstract FilePathPrefix outFilePrefixFromRoot( InputFromManager input, Path root );
	
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
}
