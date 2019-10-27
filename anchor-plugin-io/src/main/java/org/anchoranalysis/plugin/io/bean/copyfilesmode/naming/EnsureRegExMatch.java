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

import org.anchoranalysis.bean.annotation.BeanField;

/**
 * Rejects files that fail to match a particular regular-expression
 * 
 * @author FEEHANO
 *
 */
public class EnsureRegExMatch extends CopyFilesNaming {

	// START BEAN PROPERTIES
	@BeanField
	private CopyFilesNaming copyFilesNaming;
	
	@BeanField
	private String regex;
	
	/** Iff TRUE, then a file is rejected if regEx matches and vice-versa (opposite to normal behaviour) */
	@BeanField
	private boolean invert = false;
	// END BEAN PROPERTIES
	
	@Override
	public void beforeCopying(Path destDir, int totalNumFiles) {
		copyFilesNaming.beforeCopying(destDir, totalNumFiles);
	}

	@Override
	public Path destinationPathRelative(Path sourceDir, Path destDir, File file, int iter) throws IOException {
		Path path = copyFilesNaming.destinationPathRelative(sourceDir, destDir, file, iter);

		if ( path!=null && acceptPath(path) ) {
			return path;
		} else {
			return null;
		}
	}
	
	private boolean acceptPath( Path path ) {
		boolean matches = NamingUtilities.convertToString(path).matches(regex);
		
		if (invert) {
			return !matches;
		} else {
			return matches;
		}
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

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
}
