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

import java.nio.file.Path;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;


/**
 * Looks for the last directory-name, and removes it in favour of using it as a prefix on a filename
 * 
 * <p>e.g. <pre>/a/b/c/d/e/somename.tif</pre> instead becomes <pre>/a/b/c/d/e_somename.tif</pre></p>
 * 
 *  * @author owen
 *
 */
public class LastDirectoryAsPrefix extends FilePathPrefixerAvoidResolve {

	// START BEAN PROPERTIES
	@BeanField
	private FilePathPrefixerAvoidResolve filePathPrefixer;
	
	@BeanField
	private String delimiter = "_";
	// END BEAN PROPERTIES
	
	@Override
	protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root) throws AnchorIOException {
		
		FilePathPrefix fpp = filePathPrefixer.outFilePrefixFromPath(input, root);
		
		Path dir = fpp.getFolderPath();
		
		if (dir.getNameCount()>0) {
			
			String finalDirName = dir.getName( dir.getNameCount()-1 ).toString();
			
			// Remove the final directory from the output
			fpp.setFolderPath( fpp.getFolderPath().resolve("..").normalize() );
			
			if (fpp.getFilenamePrefix()!=null) {
				fpp.setFilenamePrefix( finalDirName + delimiter + fpp.getFilenamePrefix() );
			} else {
				fpp.setFilenamePrefix(finalDirName);
			}
			
			return fpp;
	
		} else {
			// Nothing to do
			return fpp;
		}
	}

	public FilePathPrefixerAvoidResolve getFilePathPrefixer() {
		return filePathPrefixer;
	}

	public void setFilePathPrefixer(FilePathPrefixerAvoidResolve filePathPrefixer) {
		this.filePathPrefixer = filePathPrefixer;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}
