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

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.path.PathDifferenceException;
import org.anchoranalysis.io.output.path.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.FilePathPrefixerContext;
import org.anchoranalysis.io.output.path.NamedPath;
import org.anchoranalysis.io.output.path.PathPrefixer;
import org.anchoranalysis.io.output.path.PathPrefixerException;
import org.anchoranalysis.plugin.io.input.path.RootPathMap;

/**
 * Prepend a 'root' before the file-path-prefix obtained from a delegate
 *
 * <p>A root is a path that is mapped via a unique-name in a settings file to a directory
 *
 * @author Owen Feehan
 */
public class Rooted extends PathPrefixer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PathPrefixerAvoidResolve filePathPrefixer;

    // The name of the root-path to associate with this fileset
    @BeanField @Getter @Setter private String rootName;
    // END BEAN PROPERTIES

    @Override
    public DirectoryWithPrefix outFilePrefix(
            NamedPath path, String expName, FilePathPrefixerContext context)
            throws PathPrefixerException {

        DirectoryWithPrefix fpp =
                filePathPrefixer.outFilePrefixAvoidResolve(
                        removeRoot(path, context.isDebugMode()), expName);

        Path pathOut = folderPathOut(fpp.getDirectory(), context.isDebugMode());
        fpp.setDirectory(pathOut);

        return fpp;
    }

    private NamedPath removeRoot(NamedPath path, boolean debugMode) throws PathPrefixerException {
        try {
            Path pathWithoutRoot =
                    RootPathMap.instance()
                            .split(path.getPath(), rootName, debugMode)
                            .getRemainder();
            return new NamedPath(path.getName(), pathWithoutRoot);
        } catch (PathDifferenceException e) {
            throw new PathPrefixerException(e);
        }
    }

    @Override
    public DirectoryWithPrefix rootFolderPrefix(String expName, FilePathPrefixerContext context)
            throws PathPrefixerException {
        DirectoryWithPrefix fpp = filePathPrefixer.rootFolderPrefixAvoidResolve(expName);
        fpp.setDirectory(folderPathOut(fpp.getDirectory(), context.isDebugMode()));
        return fpp;
    }

    private Path folderPathOut(Path pathIn, boolean debugMode) throws PathPrefixerException {
        try {
            return RootPathMap.instance().findRoot(rootName, debugMode).asPath().resolve(pathIn);
        } catch (PathDifferenceException e) {
            throw new PathPrefixerException(e);
        }
    }
}
