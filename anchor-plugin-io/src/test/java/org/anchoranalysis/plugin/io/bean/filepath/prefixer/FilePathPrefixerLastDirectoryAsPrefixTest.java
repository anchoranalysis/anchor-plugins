package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

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

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathPrefixerLastDirectoryAsPrefixTest {

	@Test
	public void test() throws AnchorIOException {
		
		Path path = Paths.get("/a/b/c/d/e/somefile.tif");
		Path root = mock(Path.class);
		String descriptiveName = "somefile";
		
		FilePathPrefixerLastDirectoryAsPrefix fpp = new FilePathPrefixerLastDirectoryAsPrefix();
		fpp.setFilePathPrefixer( createDelegate(path, descriptiveName, root) );
				
		FilePathPrefix out = fpp.outFilePrefixFromPath(path, descriptiveName, root);
		
		assertEquals( Paths.get("/g/h"), out.getFolderPath() );
		assertEquals( "i_outprefix", out.getFilenamePrefix() );
	}

	private FilePathPrefixerAvoidResolve createDelegate(Path path, String descriptiveName, Path root) throws AnchorIOException {
		FilePathPrefixerAvoidResolve fppSrc = mock(FilePathPrefixerAvoidResolve.class);
		when(fppSrc.outFilePrefixFromPath(path, descriptiveName, root)).thenReturn(
			new FilePathPrefix( Paths.get("/g/h/i/"), "outprefix")
		);
		return fppSrc;
	}
}
