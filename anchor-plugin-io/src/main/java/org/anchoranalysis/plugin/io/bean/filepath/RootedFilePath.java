package org.anchoranalysis.plugin.io.bean.filepath;

/*-
 * #%L
 * anchor-plugin-io
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.plugin.io.filepath.RootedFilePathUtilities;

public class RootedFilePath extends FilePath {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private String path;
	
	@BeanField
	private String rootName;
	
	// If TRUE, we will disable debug-mode for this current bean, if debug-mode it's set. Otherwise, there is no impact.
	@BeanField
	private boolean disableDebugMode = false;
	// END BEAN PROPERTIES
	
	@Override
	public Path path(boolean debugMode) throws IOException {
		return RootedFilePathUtilities.determineNewPath(
			Paths.get(path),
			rootName,
			debugMode,
			disableDebugMode
		);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
