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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.junit.Test;

public class RemoveExtensionsTest {

	@Test
	public void testPreserveExt() throws AnchorIOException {

		List<DescriptiveFile> df = applyTest(true);
		
		assertEquals( "d", nameFor(df,0) );
		assertEquals( "e.txt", nameFor(df,1) );
		assertEquals( "e.csv", nameFor(df,2) );
	}
	
	
	@Test(expected = AnchorIOException.class)
	public void testNonUnique() throws AnchorIOException {
		applyTest(false);
	}
	

	private List<DescriptiveFile> applyTest( boolean preserveExtension ) throws AnchorIOException {
		RemoveExtensions re = createRemoveExtensions();
		re.setPreserveExtensionIfDuplicate(preserveExtension);
		
		List<File> files = listOfFiles();
		return re.descriptiveNamesForCheckUniqueness(files, "<unknown>");
	}

	
	private RemoveExtensions createRemoveExtensions() {
		RemoveExtensions re = new RemoveExtensions();
		re.setDescriptiveName( new PatternSpan() );
		re.setPreserveExtensionIfDuplicate(true);
		return re;
	}
	
	private static List<File> listOfFiles() {
		File file1 = new File("a/b/c/d.txt");
		File file2 = new File("a/b/c/e.txt");
		File file3 = new File("a/b/c/e.csv");	// like file2 but with different extension
		
		return Arrays.asList(file1,file2,file3);		
	}
	
	private static String nameFor( List<DescriptiveFile> df, int index ) {
		return df.get(index).getDescriptiveName();
	}

}