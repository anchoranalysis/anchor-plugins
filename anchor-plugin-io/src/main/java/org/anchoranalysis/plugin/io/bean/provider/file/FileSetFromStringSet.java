package org.anchoranalysis.plugin.io.bean.provider.file;

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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.params.InputContextParams;

public class FileSetFromStringSet extends FileProviderWithDirectory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private StringSet filePaths;
	
	@BeanField
	private String directory;
	// END BEAN PROPERTIES
	
	@Override
	public Collection<File> matchingFiles(ProgressReporter progressReporter, InputContextParams inputContext)
			throws IOException {
		
		List<File> files = new ArrayList<>();
		
		Path dir = getDirectoryAsPath(inputContext);
		
		for( String s : filePaths ) {
			Path relPath = Paths.get(s);
			files.add( dir.resolve(relPath).toFile() );
		}
		
		return files;
	}

	public StringSet getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(StringSet filePaths) {
		this.filePaths = filePaths;
	}

	@Override
	public Path getDirectoryAsPath(InputContextParams inputContext) {
		return Paths.get(directory);
	
	}
	
	@Override
	public void setDirectory(Path directory) {
		this.directory = directory.toString();
	}

	public String getDirectory() {
		return directory;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
}
