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

package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public interface CopyFilesNaming {

    void beforeCopying(Path destDir, int totalNumFiles);

    /**
     * Returns the output path (destination to to be copied to) for a given single file
     *
     * @param sourceDir source-directory
     * @param destDir destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @return the absolute-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    default Optional<Path> destinationPath(Path sourceDir, Path destDir, File file, int index)
            throws OutputWriteFailedException {

        Optional<Path> remainder = destinationPathRelative(sourceDir, destDir, file, index);
        return remainder.map(destDir::resolve);
    }

    /**
     * Calculates the relative-output path (to be appended to destDir)
     *
     * @param sourceDir source-directory
     * @param destDir destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @return the relative-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int index)
            throws OutputWriteFailedException;

    void afterCopying(Path destDir, boolean dummyMode) throws OutputWriteFailedException;
}
