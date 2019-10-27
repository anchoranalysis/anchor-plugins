package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;

/**
 * Removes any whitespace characters from the path
 * 
 * @author FEEHANO
 *
 */
public class RemoveWhitespace extends CopyFilesNaming {

	// START BEAN PROPERTIES
	@BeanField
	private CopyFilesNaming copyFilesNaming;
	// END BEAN PROPERTIES

	@Override
	public void beforeCopying(Path destDir, int totalNumFiles) {
		copyFilesNaming.beforeCopying(destDir, totalNumFiles);
	}

	@Override
	public Path destinationPathRelative(Path sourceDir, Path destDir, File file, int iter) throws IOException {
		Path path = copyFilesNaming.destinationPathRelative(sourceDir, destDir, file, iter);
		
		if (path==null) {
			return null;
		}
		
		String pathMinusWhiteSpace = NamingUtilities.convertToString(path).replaceAll("\\s","");
		
		return Paths.get(pathMinusWhiteSpace);
	}

	@Override
	public void afterCopying(Path destDir, boolean dummyMode) throws IOException {
		copyFilesNaming.afterCopying(destDir, dummyMode);
	}

	public CopyFilesNaming getCopyFilesNaming() {
		return copyFilesNaming;
	}

	public void setCopyFilesNaming(CopyFilesNaming copyFilesNaming) {
		this.copyFilesNaming = copyFilesNaming;
	}
}
