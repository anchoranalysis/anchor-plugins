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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.params.InputContextParams;
import org.anchoranalysis.plugin.io.filepath.RootedFilePathUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//
// Represents a set of files, with a different root depending on the operating-system
//   and conditions
//
// The following determines which root is used, and prepended to a copy of the FileSet
//
//   If the operating system is windows:
//		* If the fileSet directory exists in localWindowsRootPath we use that
//		* Otherwise 
//
public class RootedFileSet extends FileProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PARAMETERS
	@BeanField
	private FileProviderWithDirectory fileSet;

	// The name of the RootPath to associate with this fileset
	@BeanField
	private String rootName;
	
	// If TRUE, we will disable debug-mode for this current bean, if debug-mode it's set. Otherwise, there is no impact.
	@BeanField
	private boolean disableDebugMode = false;
	// END BEAN PARAMETERS
	
	private static Log log = LogFactory.getLog(RootedFileSet.class);
	
	@Override
	public Collection<File> matchingFiles(ProgressReporter progressReporter, InputContextParams inputContext) throws IOException {
		
		try {
			log.debug( String.format("matchingFiles() old directory '%s'\n", fileSet.getDirectoryAsPath(inputContext) ));
	
			// We make a copy of the fileset
			FileProviderWithDirectory fsCopy = (FileProviderWithDirectory) fileSet.duplicateBean();
			
			Path dirOrig = fsCopy.getDirectoryAsPath(inputContext);
	
			Path dirNew = RootedFilePathUtilities.determineNewPath( dirOrig, rootName, inputContext.isDebugMode(), disableDebugMode );

			boolean dirNewExists = Files.exists(dirNew); 
					
			// As a special behaviour, if the debug folder doesn't exist, we try and again with the non-debug folder
			if (inputContext.isDebugMode() && !dirNewExists) {
				dirNew = RootedFilePathUtilities.determineNewPath( dirOrig, rootName, false, disableDebugMode );
				dirNewExists = Files.exists(dirNew);
			}
			
			if (!dirNewExists) {
				throw new IOException(
				  String.format("Path %s' does not exist", dirNew)
				);
			}
			
			log.debug( String.format("Setting new directory '%s'%n", dirNew) );
			fsCopy.setDirectory( dirNew );
			
			return fsCopy.matchingFiles(progressReporter, inputContext);
			
		} catch (BeanDuplicateException e) {
			throw new IOException(e);
		}
	}
	
	public FileProviderWithDirectory getFileSet() {
		return fileSet;
	}


	public void setFileSet(FileProviderWithDirectory fileSet) {
		this.fileSet = fileSet;
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public boolean isDisableDebugMode() {
		return disableDebugMode;
	}

	public void setDisableDebugMode(boolean disableDebugMode) {
		this.disableDebugMode = disableDebugMode;
	}
}
