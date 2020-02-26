package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

/*-
 * #%L
 * anchor-plugin-quick
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class FilePathGeneratorRemoveTrailingDir extends FilePathGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator filePathGenerator;
		
	// If non-zero, n trailing directories are removed from the end 
	@BeanField
	private int trimTrailingDirectory = 0;
	
	// Do not apply the trim operation to the first n dirs
	@BeanField
	private int skipFirstTrim = 0;
	// END BEAN PROPERTIES

	@Override
	public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
		Path path = filePathGenerator.outFilePath(pathIn, debugMode);
		
		if (trimTrailingDirectory > 0) {
			return removeNTrailingDirs(path, trimTrailingDirectory, skipFirstTrim );
		} else {
			return path;
		}
	}
	
	private Path removeNTrailingDirs( Path path, int n, int skipFirstTrim ) throws AnchorIOException {
		PathTwoParts pathDir = new PathTwoParts(path);
			
		for( int i=0; i<skipFirstTrim; i++ ) {
			pathDir.moveLastDirToRest();
		}
		
		for( int i=0; i<n; i++ ) {
			pathDir.removeLastDir();
		}
		
		return pathDir.combine();
	}
	
	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}

	public int getTrimTrailingDirectory() {
		return trimTrailingDirectory;
	}

	public void setTrimTrailingDirectory(int trimTrailingDirectory) {
		this.trimTrailingDirectory = trimTrailingDirectory;
	}

	public int getSkipFirstTrim() {
		return skipFirstTrim;
	}

	public void setSkipFirstTrim(int skipFirstTrim) {
		this.skipFirstTrim = skipFirstTrim;
	}
}
