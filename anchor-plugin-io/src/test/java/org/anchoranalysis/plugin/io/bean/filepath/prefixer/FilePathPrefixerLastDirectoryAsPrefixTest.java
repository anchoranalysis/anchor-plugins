package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

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
