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
import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.io.error.AnchorIOException;

public abstract class CopyFilesNaming {
	
	public abstract void beforeCopying(Path destDir, int totalNumFiles);
	
	/**
	 * Returns the output path (destination to to be copied to) for a given single file
	 * 
	 * @param sourceDir
	 * @param destDir
	 * @param file
	 * @param iter
	 * @return the absolute-path. if empty, the file should be skipped.
	 * @throws AnchorIOException TODO
	 */
	public Optional<Path> destinationPath(Path sourceDir, Path destDir, File file, int iter) throws AnchorIOException {
		
		Optional<Path> remainder = destinationPathRelative( sourceDir, destDir, file, iter );
		return remainder.map(r->
			destDir.resolve(r)
		);
	}
	
	/**
	 * Calculates the relative-output path (to be appended to destDir
	 * 
	 * @param sourceDir
	 * @param destDir
	 * @param file
	 * @param iter
	 * @return the relative-path. if empty, the file should be skipped.
	 * @throws AnchorIOException TODO
	 */
	public abstract Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter) throws AnchorIOException;
	
	public abstract void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException;
}
