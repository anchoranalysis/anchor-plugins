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
import org.anchoranalysis.io.path.DerivePathException;
import org.anchoranalysis.io.path.NamedPath;
import org.anchoranalysis.io.path.prefixer.DirectoryWithPrefix;

/**
 * Looks for the last directory-name, and removes it in favour of using it as a prefix on a filename
 *
 * <p>e.g.
 *
 * <pre>/a/b/c/d/e/somename.tif</pre>
 *
 * instead becomes
 *
 * <pre>/a/b/c/d/e_somename.tif</pre>
 *
 * * @author Owen Feehan
 */
public class LastDirectoryAsPrefix extends PathPrefixerAvoidResolve {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PathPrefixerAvoidResolve filePathPrefixer;

    @BeanField @Getter @Setter private String delimiter = "_";
    // END BEAN PROPERTIES

    @Override
    protected DirectoryWithPrefix outFilePrefixFromPath(NamedPath path, Path root)
            throws DerivePathException {

        DirectoryWithPrefix prefix = filePathPrefixer.outFilePrefixFromPath(path, root);

        Path dir = prefix.getDirectory();

        if (dir.getNameCount() > 0) {

            String finalDirName = dir.getName(dir.getNameCount() - 1).toString();

            // Remove the final directory from the output
            prefix.setDirectory(prefix.getDirectory().resolve("..").normalize());

            if (prefix.getFilenamePrefix() != null) {
                prefix.setFilenamePrefix(finalDirName + delimiter + prefix.getFilenamePrefix());
            } else {
                prefix.setFilenamePrefix(finalDirName);
            }

            return prefix;

        } else {
            // Nothing to do
            return prefix;
        }
    }
}
