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
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

public abstract class FilePathRslvrBase extends FilePathPrefixerAvoidResolve {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @AllowEmpty
	private String inPathPrefix = "";
	
	@BeanField
	private String outPathPrefix = "";
	
	@BeanField
	private boolean fileAsFolder = true;
	// END BEAN PROPERTIES
	
	public String getInPathPrefix() {
		return inPathPrefix;
	}
	
	public Path getInPathPrefixAsPath() {
		return Paths.get(inPathPrefix);
	}

	public void setInPathPrefix(String inPathPrefix) {
		this.inPathPrefix = inPathPrefix;
	}

	public String getOutPathPrefix() {
		return outPathPrefix;
	}

	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}

	public boolean isFileAsFolder() {
		return fileAsFolder;
	}

	public void setFileAsFolder(boolean fileAsFolder) {
		this.fileAsFolder = fileAsFolder;
	}	
	

	
	@Override
	public FilePathPrefix rootFolderPrefix(String experimentIdentifier, boolean debugMode) throws IOException {
		String outPathAbsolute = resolvePath( Paths.get(outPathPrefix) ).toString();
		return HelperFilePathRslvr.createRootFolderPrefix( outPathAbsolute, experimentIdentifier );
	}

	
	// the root folder we write to, without any file specific additions
	@Override
	public FilePathPrefix rootFolderPrefixAvoidResolve( String experimentIdentifier )  {
		return HelperFilePathRslvr.createRootFolderPrefix( this.outPathPrefix, experimentIdentifier );
	}
}
