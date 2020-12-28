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
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * How an output name (and path) is selected for an input file when copying.
 *
 * @author Owen Feehan
 * @param <T> shared-state
 */
public abstract class CopyFilesNaming<T> extends AnchorBean<CopyFilesNaming<T>> {

    /**
     * To be called <i>once</i> before any calls to {@link #destinationPath(Path, Path, File, int,
     * Object)}.
     *
     * @param destinationDirectory the directory to which files are copied.
     * @param totalNumberFiles the total number of files to copy.
     */
    public abstract T beforeCopying(Path destinationDirectory, int totalNumberFiles);

    /**
     * Returns the output path (destination to to be copied to) for a given single file.
     *
     * @param sourceDirectory source-directory
     * @param destinationDirectory destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @return the absolute-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    public Optional<Path> destinationPath(
            Path sourceDirectory, Path destinationDirectory, File file, int index, T sharedState)
            throws OutputWriteFailedException {

        Optional<Path> remainder =
                destinationPathRelative(
                        sourceDirectory, destinationDirectory, file, index, sharedState);
        return remainder.map(destinationDirectory::resolve);
    }

    /**
     * Calculates the relative-output path (to be appended to destDir)
     *
     * @param sourceDirectory source-directory
     * @param destinationDirectory destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @param sharedState
     * @return the relative-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    public abstract Optional<Path> destinationPathRelative(
            Path sourceDirectory, Path destinationDirectory, File file, int index, T sharedState)
            throws OutputWriteFailedException;
}
