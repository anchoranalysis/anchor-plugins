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

package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.input.path.CopyContext;

/**
 * How an output name (and path) is selected for an input file when copying.
 *
 * @author Owen Feehan
 * @param <T> shared-state
 */
public abstract class CopyFilesNaming<T> extends AnchorBean<CopyFilesNaming<T>> {

    /**
     * To be called <i>once</i> before any calls to {@link #destinationPath(File,
     * DirectoryWithPrefix, int, CopyContext)}.
     *
     * @param destinationDirectory the directory to which files are copied.
     * @param inputs the total number of files to copy.
     */
    public abstract T beforeCopying(Path destinationDirectory, List<FileWithDirectoryInput> inputs)
            throws OperationFailedException;

    /**
     * Returns the output path (destination to to be copied to) for a given single file.
     *
     * @param file file to be copied
     * @param outputTarget the directory and prefix associated with the file for outputting
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @param context the context for the copying
     * @return the absolute-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    public Optional<Path> destinationPath(
            File file, DirectoryWithPrefix outputTarget, int index, CopyContext<T> context)
            throws OutputWriteFailedException {

        Optional<Path> remainder = destinationPathRelative(file, outputTarget, index, context);
        return remainder.map(context.getDestinationDirectory()::resolve);
    }

    /**
     * Calculates the relative-output path (to be appended to destDir)
     *
     * @param file file to be copied
     * @param outputTarget the directory and prefix associated with the file for outputting
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @param context the context for the copying
     * @return the relative-path. if empty, the file should be skipped.
     * @throws OutputWriteFailedException
     */
    public abstract Optional<Path> destinationPathRelative(
            File file, DirectoryWithPrefix outputTarget, int index, CopyContext<T> context)
            throws OutputWriteFailedException;
}
