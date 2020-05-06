package org.anchoranalysis.plugin.annotation.bean.strategy;

/*-
 * #%L
 * anchor-plugin-annotation
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

import java.nio.file.Path;

import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class GeneratorPathRslvr {

	/** The debug-mode for everything that isn't the main input */
	private final static boolean DEBUG_MODE_NON_INPUT = false;
	
	private Path pathForBinding;
	

	public GeneratorPathRslvr(Path pathForBinding) {
		super();
		this.pathForBinding = pathForBinding;
	}	
	
	public Path path( FilePathGenerator generator ) throws AnchorIOException {
		return generator.outFilePath( pathForBinding, DEBUG_MODE_NON_INPUT );
	}
	
	public Path pathOrNull( FilePathGenerator generator ) throws AnchorIOException {
		if (generator!=null) {
			return path( generator );
		} else {
			return null;
		}
	}
}
