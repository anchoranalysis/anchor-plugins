/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.filepath;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class FilePathGeneratorRemoveTrailingDir extends FilePathGenerator {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;

    // If non-zero, n trailing directories are removed from the end
    @BeanField @Getter @Setter private int trimTrailingDirectory = 0;

    // Do not apply the trim operation to the first n dirs
    @BeanField @Getter @Setter private int skipFirstTrim = 0;
    // END BEAN PROPERTIES

    @Override
    public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
        Path path = filePathGenerator.outFilePath(pathIn, debugMode);

        if (trimTrailingDirectory > 0) {
            return removeNTrailingDirs(path, trimTrailingDirectory, skipFirstTrim);
        } else {
            return path;
        }
    }

    private Path removeNTrailingDirs(Path path, int n, int skipFirstTrim) throws AnchorIOException {
        PathTwoParts pathDir = new PathTwoParts(path);

        for (int i = 0; i < skipFirstTrim; i++) {
            pathDir.moveLastDirectoryToRest();
        }

        for (int i = 0; i < n; i++) {
            pathDir.removeLastDirectory();
        }

        return pathDir.combine();
    }
}
