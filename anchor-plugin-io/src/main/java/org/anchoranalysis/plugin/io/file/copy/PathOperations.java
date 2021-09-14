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

package org.anchoranalysis.plugin.io.file.copy;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.core.system.path.PathDifference;
import org.anchoranalysis.core.system.path.PathDifferenceException;

/**
 * Helper routines for performing operations on {@link Path}s.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathOperations {

    /**
     * Finds the difference between a path and a base.
     *
     * @param baseDirectoryPath path to a base directory.
     * @param filePath the path to resolve.
     * @return the difference between the paths.
     * @throws PathDifferenceException if the canonical file cannot be found.
     */
    public static Path filePathDifference(Path baseDirectoryPath, Path filePath)
            throws PathDifferenceException {
        PathDifference difference = PathDifference.differenceFrom(baseDirectoryPath, filePath);
        return difference.combined();
    }

    /**
     * Converts a path to a string, making sure it uses UNIX-style separators.
     *
     * @param path the path to convert.
     * @return a converted path, with UNIX-style separators.
     */
    public static String convertToString(Path path) {
        return FilePathToUnixStyleConverter.toStringUnixStyle(path);
    }
}
