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

package org.anchoranalysis.plugin.io.bean.file.path.prefixer;

import java.nio.file.Path;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixerAvoidResolve;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerException;
import org.anchoranalysis.plugin.io.input.path.RootPathMap;

/**
 * Prepend a 'root' before the file-path-prefix obtained from a delegate
 *
 * <p>A root is a path that is mapped via a unique-name in a settings file to a directory
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class Rooted extends PathPrefixer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PathPrefixerAvoidResolve prefixer;

    /** The name of the root-path to associate with this fileset. */
    @BeanField @Getter @Setter private String rootName;

    // END BEAN PROPERTIES

    @Override
    public DirectoryWithPrefix outFilePrefix(
            NamedPath path, Optional<String> experimentName, PathPrefixerContext context)
            throws PathPrefixerException {

        DirectoryWithPrefix directoryWithPrefix =
                prefixer.outFilePrefixAvoidResolve(
                        removeRoot(path, context.isDebugMode()), experimentName, context);

        Path pathOut = folderPathOut(directoryWithPrefix.getDirectory(), context.isDebugMode());
        directoryWithPrefix.setDirectory(pathOut);

        return directoryWithPrefix;
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
    public DirectoryWithPrefix rootDirectoryPrefix(
            Optional<String> expName, PathPrefixerContext context) throws PathPrefixerException {
        DirectoryWithPrefix fpp = prefixer.rootDirectoryPrefixAvoidResolve(expName);
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
