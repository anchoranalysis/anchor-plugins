package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

/*-
 * #%L
 * anchor-plugin-quick
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

/** Seperates a path into two parts */
class PathTwoParts {
	private Path first;
	private Path second;
	
	public PathTwoParts(Path path) throws IOException {
		checkPathHasParent(path);
		
		if ( path.endsWith("/") ) {
			first = path;
			second = null;
		} else {
			first = path.getParent();
			second = path.getFileName();
		}
	}
	
	/** Removes the last directory from the dir */
	public void removeLastDir() {
		first = first.getParent();
	}
	
	/** Removes the last directory in dir to rest  */
	public void moveLastDirToRest() {
		Path name = first.getFileName();
		second = name.resolve(second);
		first = first.getParent();
	}
	
	public Path combine() {
		return first.resolve(second);
	}
			
	private static void checkPathHasParent( Path path ) throws IOException {
		
		if (path.getParent()==null) {
			throw new IOException( String.format("No parent exists for %s", path ) );
		}
	}

	public Path getFirst() {
		return first;
	}

	public Path getSecond() {
		return second;
	}
}
