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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

// 
public class HomeSubdirectory extends FilePathPrefixer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START PROPERTIES
	@BeanField
	private String subDirectoryName = "";
	
	@BeanField
	private String anchorDirectory = "anchor";
	// END PROPERTIES

	// If delegate is null, it means it hasn't been initialised yet
	private FilePathCounter delegate;
	
	private void initIfPossible() throws InitException {
		if (delegate==null) {
			String strHomeDir = System.getProperty("user.home");
			
			if (strHomeDir==null || strHomeDir.isEmpty()) {
				throw new InitException("No user.home environmental variable");
			}
			
			Path pathHomeDir = Paths.get(strHomeDir);
			
			if (!Files.exists(pathHomeDir)) {
				throw new InitException( String.format("User home dir '%s' does not exist",strHomeDir) );
			}
			
			Path anchorFolder = pathHomeDir.resolve(anchorDirectory);
			
			if (!Files.exists(anchorFolder)) {
				try {
					Files.createDirectory(anchorFolder);
				} catch (IOException e) {
					throw new InitException(e);
				}
			}
			
			Path pathSubfolder = anchorFolder.resolve(subDirectoryName);
			
			if (!Files.exists(pathSubfolder)) {
				try {
					Files.createDirectory(pathSubfolder);
				} catch (IOException e) {
					throw new InitException(e);
				}
			}
			
			delegate = new FilePathCounter( pathSubfolder.toString() );
			
			// We localize instead to the home sub-directory, not to the current bean location
			try {
				delegate.localise( pathSubfolder );
			} catch (BeanMisconfiguredException e) {
				throw new InitException(e);
			}
		}
	}

	public HomeSubdirectory() {
		
	}
	
	@Override
	public FilePathPrefix outFilePrefix(Path pathIn, String expName, boolean debugMode)
			throws IOException {
		try {
			initIfPossible();
		} catch (InitException e) {
			throw new IOException(e);
		}
		return delegate.outFilePrefix(pathIn, expName, debugMode);
	}

	@Override
	public FilePathPrefix rootFolderPrefix(String expName, boolean debugMode) throws IOException {
		try {
			initIfPossible();
		} catch (InitException e) {
			throw new IOException(e);
		}
		return delegate.rootFolderPrefix(expName, debugMode);
	}

	public String getSubDirectoryName() {
		return subDirectoryName;
	}

	public void setSubDirectoryName(String subDirectoryName) {
		this.subDirectoryName = subDirectoryName;
	}

	public String getAnchorDirectory() {
		return anchorDirectory;
	}

	public void setAnchorDirectory(String anchorDirectory) {
		this.anchorDirectory = anchorDirectory;
	}	
}
