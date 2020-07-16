/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.junit.Test;

public class FilePathPrefixerLastDirectoryAsPrefixTest {

    @Test
    public void test() throws FilePathPrefixerException {

        Path root = mock(Path.class);

        PathWithDescription input =
                new PathWithDescription(Paths.get("/a/b/c/d/e/somefile.tif"), "somefile");

        LastDirectoryAsPrefix fpp = new LastDirectoryAsPrefix();
        fpp.setFilePathPrefixer(createDelegate(input, root));

        FilePathPrefix out = fpp.outFilePrefixFromPath(input, root);

        assertEquals(Paths.get("/g/h"), out.getFolderPath());
        assertEquals("i_outprefix", out.getFilenamePrefix());
    }

    private FilePathPrefixerAvoidResolve createDelegate(PathWithDescription input, Path root)
            throws FilePathPrefixerException {
        FilePathPrefixerAvoidResolve fppSrc = mock(FilePathPrefixerAvoidResolve.class);
        when(fppSrc.outFilePrefixFromPath(input, root))
                .thenReturn(new FilePathPrefix(Paths.get("/g/h/i/"), "outprefix"));
        return fppSrc;
    }
}
