package org.anchoranalysis.plugin.io.bean.rasterreader;

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
import java.nio.file.Path;
import java.util.Iterator;

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.plugin.io.bean.groupfiles.parser.FilePathParser;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

// Expects to be passed one file per set
// Then finds all channels and stacks from an associated regular expression
//   that is matched against files in the directory
public class MultiFileReader extends RasterReader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -817112855285215792L;

	// START BEAN PROPERTIES
	@BeanField
	private FilePathParser filePathParser = null;
	
	@BeanField @DefaultInstance
	private RasterReader rasterReader = null;
	
	@BeanField
	private boolean recurseSubfolders = false;
	
	/** Search x number directories higher than file */
	@BeanField
	private int navigateHigherDirs = 0;
	
	/** If non-empty a regular-expression is applied to files */
	@BeanField @AllowEmpty
	private String regExFile = "";
	
	/** If non-empty a regular-expression is applied to directories */
	@BeanField @AllowEmpty
	private String regExDir = "";
	// END BEAN PROPERTIES
	
	@Override
	public OpenedRaster openFile(Path filePath) throws RasterIOException {
		
		// We look at all other files in the same folder as our filepath to match our expression
		
		Iterator<File> fileIterator = FileUtils.iterateFiles(
			folderFromFile(filePath),
			maybeRegExFilter(regExFile),
			recurseFilter()
		);
		
		ParsedFilePathBag bag = new ParsedFilePathBag();
		
		while (fileIterator.hasNext()) {
			File f = fileIterator.next();

			if (filePathParser.setPath( f.getAbsolutePath())) {
				
				bag.add(
					new FileDetails(
						f.toPath().toAbsolutePath(),
						filePathParser.getChnlNum(),
						filePathParser.getZSliceNum(),
						filePathParser.getTimeIndex()
					)
				);
			}
		}
		
		return new MultiFileReaderOpenedRaster( rasterReader, bag );
	}
	
	private File folderFromFile( Path filePath ) {
		
		File dir = filePath.toFile();
		
		if (!filePath.toFile().isDirectory()) {
			// If we start with a file-path we first go to the parent directory
			dir = dir.getParentFile();
		}
				
		for( int i=0; i<navigateHigherDirs; i++) {
			dir = dir.getParentFile();
		}
		
		return dir;
	}
		
	private IOFileFilter recurseFilter() {
		return recurseSubfolders ? maybeRegExFilter(regExDir) : null;
	}
	
	private static IOFileFilter maybeRegExFilter( String regEx ) {
		if (!regEx.isEmpty()) {
			return new RegexFileFilter(regEx);
		} else {
			return TrueFileFilter.INSTANCE;
		}
	}

	
	public FilePathParser getFilePathParser() {
		return filePathParser;
	}

	public void setFilePathParser(FilePathParser filePathParser) {
		this.filePathParser = filePathParser;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public boolean isRecurseSubfolders() {
		return recurseSubfolders;
	}

	public void setRecurseSubfolders(boolean recurseSubfolders) {
		this.recurseSubfolders = recurseSubfolders;
	}

	public int getNavigateHigherDirs() {
		return navigateHigherDirs;
	}

	public void setNavigateHigherDirs(int navigateHigherDirs) {
		this.navigateHigherDirs = navigateHigherDirs;
	}

	public String getRegExDir() {
		return regExDir;
	}

	public void setRegExDir(String regExDir) {
		this.regExDir = regExDir;
	}

	public String getRegExFile() {
		return regExFile;
	}

	public void setRegExFile(String regExFile) {
		this.regExFile = regExFile;
	}

}
