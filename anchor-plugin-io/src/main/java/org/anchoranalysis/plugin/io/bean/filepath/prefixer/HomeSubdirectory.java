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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.io.output.path.DerivePathException;
import org.anchoranalysis.io.output.path.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.FilePathPrefixerContext;
import org.anchoranalysis.io.output.path.NamedPath;
import org.anchoranalysis.io.output.path.PathPrefixer;

//
public class HomeSubdirectory extends PathPrefixer {

    // START PROPERTIES
    /** A relative-path (to the user home directory) which is used as an output directory */
    @BeanField @Getter @Setter private String directory = "anchorData";
    // END PROPERTIES

    // If delegate is null, it means it hasn't been initialised yet
    private FilePathCounter delegate;

    private void initIfPossible() throws InitException {
        if (delegate == null) {

            Path pathAnchorDir = createSubdirectoryIfNecessary(homeDir(), directory);

            delegate = new FilePathCounter(pathAnchorDir.toString());

            // We localize instead to the home subdirectory, not to the current bean location
            try {
                delegate.localise(pathAnchorDir);
            } catch (BeanMisconfiguredException e) {
                throw new InitException(e);
            }
        }
    }

    @Override
    public DirectoryWithPrefix outFilePrefix(
            NamedPath path, String expName, FilePathPrefixerContext context)
            throws DerivePathException {
        try {
            initIfPossible();
        } catch (InitException e) {
            throw new DerivePathException(e);
        }
        return delegate.outFilePrefix(path, expName, context);
    }

    @Override
    public DirectoryWithPrefix rootFolderPrefix(String expName, FilePathPrefixerContext context)
            throws DerivePathException {
        try {
            initIfPossible();
        } catch (InitException e) {
            throw new DerivePathException(e);
        }
        return delegate.rootFolderPrefix(expName, context);
    }

    private Path homeDir() throws InitException {
        String strHomeDir = System.getProperty("user.home");

        if (strHomeDir == null || strHomeDir.isEmpty()) {
            throw new InitException("No user.home environmental variable");
        }

        Path pathHomeDir = Paths.get(strHomeDir);

        if (!pathHomeDir.toFile().exists()) {
            throw new InitException(String.format("User home dir '%s' does not exist", strHomeDir));
        }

        return pathHomeDir;
    }

    private Path createSubdirectoryIfNecessary(Path pathHomeDir, String relativePathSubdirectory)
            throws InitException {
        Path resolvedPath = pathHomeDir.resolve(relativePathSubdirectory);
        try {
            if (!resolvedPath.toFile().exists()) {
                Files.createDirectory(resolvedPath);
            }
        } catch (IOException e) {
            throw new InitException(e);
        }
        return resolvedPath;
    }
}
