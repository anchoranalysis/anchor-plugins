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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.input.InputFromManager;

public class FilePathCounter extends FilePathPrefixerSpecifiedOutPath {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int cnt = 0;
	
	// START BEAN PROPERTIES
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
		super(outPathPrefix);
	}

	@Override
	protected FilePathPrefix outFilePrefixFromRoot(InputFromManager input, Path root) {
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
	
	public int getNumLeadingZeros() {
		return numLeadingZeros;
	}

	public void setNumLeadingZeros(int numLeadingZeros) {
		this.numLeadingZeros = numLeadingZeros;
	}

}