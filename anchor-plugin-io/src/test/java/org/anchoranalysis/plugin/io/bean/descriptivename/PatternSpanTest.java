package org.anchoranalysis.plugin.io.bean.descriptivename;

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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.patternspan.PatternSpan;
import org.junit.Test;

public class PatternSpanTest {

	@Test
	public void testSimple() throws AnchorIOException {
		
		String inputs[] = {
			"/a/b/c.txt",
			"/a/d/c.txt",
			"/a/e/c.txt"
		};

		String expected[] = {"b", "d", "e"};
		applyTest(inputs, expected);
	}
	
	@Test
	public void testPaths() throws AnchorIOException {
		String inputs[] = {
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/Klosters (Feb 2020)/P1210904.JPG"
		};
		
		String expected[] = {
			"P1210940",
			"Klosters (Feb 2020)/P1210904"
		};

		applyTest(inputs, expected);
	}
	
	@Test
	public void testEmptyStr() throws AnchorIOException {
		String inputs[] = {
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG.TXT"
		};
		
		String expected[] = {
			"P1210940.JPG",
			"P1210940.JPG.TXT"
		};

		applyTest(inputs, expected);
	}
	
	// When there is no extension, the right-side should be kept
	@Test
	public void testWithoutExtension() throws AnchorIOException {
		
		String inputs[] = {
			"/a/b/c",
			"/a/d/c",
			"/a/e/c"
		};

		String expected[] = {"b/c", "d/c", "e/c"};
		applyTest(inputs, expected);
	}
	
	private static void applyTest( String[] paths, String[] expected ) {
		List<File> files = filesFromStrs(paths);
		
		PatternSpan ps = new PatternSpan();
		List<DescriptiveFile> ret = ps.descriptiveNamesFor(files, "unknown");
		
		for( int i=0; i<expected.length; i++ ) {
			assertIndexEquals( ret, i, expected[i]);
		}
	}
	
	private static List<File> filesFromStrs( String[] paths ) {
		return Arrays.stream(paths).map(File::new).collect( Collectors.toList() );
	}
	
	private static void assertIndexEquals( List<DescriptiveFile> ret, int index, String expected ) {
		String actual = ret.get(index).getDescriptiveName();
		assertEquals( expected, actual );
	}
}
