package org.anchoranalysis.plugin.io.bean.summarizer.path;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.core.error.OperationFailedException;
import org.junit.Test;

public class FilePathPatternTest {

	@Test
	public void test() throws OperationFailedException {
		
		Path path1 = Paths.get("a/b/c");
		Path path2 = Paths.get("a/b/d");
		
		FilePathPattern fpp = new FilePathPattern();
		fpp.add( path1 );
		fpp.add( path2 );

		assertEquals(
			"${0} = \"d\" (1) | \"c\" (1)",
			secondLineFrom( fpp.describe() )
		);
	}
	
	private static String secondLineFrom( String multilineStr ) {
		String[] split = multilineStr.split( System.getProperty("line.separator") );
		return split[1];
	}

}
