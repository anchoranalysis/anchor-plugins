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
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.NamedPath;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.PathDifferenceFromBase;
import org.apache.commons.io.FilenameUtils;

/**
 * Reuses the directories between a path and its root to form the output - and also the filename.
 *
 * <p>e.g. for a path=
 *
 * <pre>/a/b/c/d/e.tif</pre>
 *
 * and root=
 *
 * <pre>/a/b</pre>
 *
 * then the prefix would be
 *
 * <pre>c/d/e/</pre>
 *
 * @author Owen Feehan
 */
public class DirectoryStructure extends FilePathPrefixerAvoidResolve {

    // START BEAN PROPERTIES
    /** If false, the folders are ignored, and only the file-name is used in the output */
    @BeanField @Getter @Setter private boolean includeFolders = true;

    @BeanField @AllowEmpty @Getter @Setter private String inPathPrefix = "";
    // END BEAN PROPERTIES

    @Override
    protected FilePathPrefix outFilePrefixFromPath(NamedPath path, Path root)
            throws FilePathPrefixerException {

        PathDifferenceFromBase difference = differenceToPrefix(removeExtension(path.getPath()));

        return new FilePathPrefix(buildOutPath(root, difference));
    }

    private static Path removeExtension(Path withExtension) {
        String pathWithExtension = withExtension.toString();
        return Paths.get(FilenameUtils.removeExtension(pathWithExtension));
    }

    private PathDifferenceFromBase differenceToPrefix(Path pathInRemoved)
            throws FilePathPrefixerException {
        try {
            return PathDifferenceFromBase.differenceFrom(Paths.get(inPathPrefix), pathInRemoved);
        } catch (AnchorIOException e) {
            throw new FilePathPrefixerException(e);
        }
    }

    private Path buildOutPath(Path root, PathDifferenceFromBase ff) {
        if (includeFolders) {
            return root.resolve(ff.combined());
        } else {
            return root.resolve(ff.getFilename());
        }
    }
}
