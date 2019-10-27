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

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

public class FilePathCounter extends FilePathPrefixer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int cnt = 0;
	
	// START BEAN PROPERTIES
	@BeanField
	private String outPathPrefix = "";
	
	@BeanField @AllowEmpty
	private String outFilePrefix = "";
	
	@BeanField
	private int numLeadingZeros = 4;
	
	/**
	 * If true, a subfolder is created for each incremented count. If false, a prefix is used instead
	 */
	@BeanField
	private boolean subfolder = true;
	// END BEAN PROPERTIES
	
	public FilePathCounter() {
		// BEAN CONSTRUCTOR
	}
	
	public FilePathCounter( String outPathPrefix ) {
		this.outFilePrefix = outPathPrefix;
	}
	
	@Override
	public FilePathPrefix outFilePrefix(Path pathIn, String expName, boolean debugMode)
			throws IOException {

		Path root = rootFolderPrefix(expName, debugMode).getFolderPath();
		
		
		String formatSpecifier = "%0" + numLeadingZeros + "d";
		String identifier = String.format( formatSpecifier, cnt++ );
		
		if (subfolder) {
			Path combinedDir = root.resolve(identifier);
			FilePathPrefix fpp = new FilePathPrefix(combinedDir);
			fpp.setFilenamePrefix("");
			return fpp;
		} else {
			FilePathPrefix fpp = new FilePathPrefix( root );
			fpp.setFilenamePrefix( identifier + "_" );
			return fpp;
		}
	}

	@Override
	public FilePathPrefix rootFolderPrefix(String expName, boolean debugMode) {
		Path folder = getResolvedOutPathPrefix().resolve(expName);
		return new FilePathPrefix(folder);
	}
	
	private Path getResolvedOutPathPrefix() {
		return resolvePath( Paths.get(outPathPrefix) );
	}

	public String getOutPathPrefix() {
		return outPathPrefix;
	}

	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}

	public int getNumLeadingZeros() {
		return numLeadingZeros;
	}

	public void setNumLeadingZeros(int numLeadingZeros) {
		this.numLeadingZeros = numLeadingZeros;
	}

	public String getOutFilePrefix() {
		return outFilePrefix;
	}

	public void setOutFilePrefix(String outFilePrefix) {
		this.outFilePrefix = outFilePrefix;
	}

}
