/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixerAvoidResolve;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerException;
import org.junit.Test;

public class FilePathPrefixerLastDirectoryAsPrefixTest {

    @Test
    public void test() throws PathPrefixerException {

        Path root = mock(Path.class);

        NamedPath path = new NamedPath("somefile", Paths.get("/a/b/c/d/e/somefile.tif"));

        PathPrefixerContext context = new PathPrefixerContext();
        
        LastDirectoryAsPrefix prefixer = new LastDirectoryAsPrefix();
        prefixer.setPrefixer(createDelegate(path, root, context));

        DirectoryWithPrefix out = prefixer.outFilePrefixFromPath(path, root, context);

        assertEquals(Paths.get("/g/h"), out.getDirectory());
        assertEquals("i_", out.prefixWithDelimeter());
    }

    private PathPrefixerAvoidResolve createDelegate(NamedPath path, Path root, PathPrefixerContext context)
            throws PathPrefixerException {
        PathPrefixerAvoidResolve prefixer = mock(PathPrefixerAvoidResolve.class);
        when(prefixer.outFilePrefixFromPath(path, root, context))
                .thenReturn(new DirectoryWithPrefix(Paths.get("/g/h/i/"), "", "_"));
        return prefixer;
    }
}
