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
import java.nio.file.Path;
import java.util.Collection;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
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
public class Rooted extends FileProvider {

	// START BEAN PARAMETERS
	@BeanField
	private FileProviderWithDirectory fileProvider;

	// The name of the RootPath to associate with this fileset
	@BeanField
	private String rootName;
	
	// If TRUE, we will disable debug-mode for this current bean, if debug-mode it's set. Otherwise, there is no impact.
	@BeanField
	private boolean disableDebugMode = false;
	// END BEAN PARAMETERS
	
	private static Log log = LogFactory.getLog(Rooted.class);
	
	@Override
	public Collection<File> create(InputManagerParams params) throws FileProviderException {
		
		try {
			log.debug( String.format("matchingFiles() old directory '%s'%n", fileProvider.getDirectoryAsPath(params.getInputContext()) ));
			
			Path dirOrig = fileProvider.getDirectoryAsPath(params.getInputContext());
	
			Path dirNew = RootedFilePathUtilities.determineNewPath(
				dirOrig,
				rootName,
				params.isDebugModeActivated(),
				disableDebugMode
			);

			boolean dirNewExists = dirNew.toFile().exists(); 
					
			// As a special behaviour, if the debug folder doesn't exist
			// we try and again with the non-debug folder
			if (params.isDebugModeActivated() && !dirNewExists) {
				dirNew = RootedFilePathUtilities.determineNewPath(
					dirOrig,
					rootName,
					false,
					disableDebugMode
				);
				dirNewExists = dirNew.toFile().exists();
			}
			
			if (!dirNewExists) {
				throw new FileProviderException(
				  String.format("Path %s' does not exist", dirNew)
				);
			}
			
			log.debug( String.format("Setting new directory '%s'%n", dirNew) );
			
			return fileProvider.matchingFilesForDirectory(dirNew, params);
			
		} catch (BeanDuplicateException | AnchorIOException e) {
			throw new FileProviderException(e);
		}
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

	public FileProviderWithDirectory getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProviderWithDirectory fileProvider) {
		this.fileProvider = fileProvider;
	}
}
