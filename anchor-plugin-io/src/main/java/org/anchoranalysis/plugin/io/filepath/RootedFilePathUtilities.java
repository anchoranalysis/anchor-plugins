package org.anchoranalysis.plugin.io.filepath;

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

import org.anchoranalysis.io.bean.root.RootPath;
import org.anchoranalysis.io.bean.root.RootPathMap;

public class RootedFilePathUtilities {

	public static Path determineNewPath( Path pathOrig, String rootName, boolean debugMode, boolean disableDebugMode ) throws IOException {
		
		// If forceServer is selected, we disable debug-mode
		boolean effectiveDebugMode = debugMode;
		if (disableDebugMode) {
			effectiveDebugMode = false;
		}
		
		RootPath root = RootPathMap.instance().findRoot( rootName, effectiveDebugMode );
		return root.asPath().resolve( pathOrig );
	}
}
