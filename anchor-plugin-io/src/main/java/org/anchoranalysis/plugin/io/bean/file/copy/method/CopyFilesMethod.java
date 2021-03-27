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

package org.anchoranalysis.plugin.io.bean.file.copy.method;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.CreateException;

/**
 * A method used for copying files (e.g. bytewise or activating compression etc.).
 *
 * @author Owen Feehan
 */
public abstract class CopyFilesMethod extends AnchorBean<CopyFilesMethod> {

    /**
     * Makes a copy of the file at {@code source} at {@code destination}.
     *
     * @param source the path of the file to copy from
     * @param destination the path to create a copy at
     * @throws CreateException if anything goes wrong
     */
    public void makeCopy(Path source, Path destination) throws CreateException {
        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new CreateException(e);
        }
        makeCopyWithDirectory(source, destination);
    }

    /**
     * Like {@link #makeCopy(Path,Path)} but after any necessary directories are created so {@code
     * destination} can be written.
     *
     * @param source
     * @param destination
     * @throws CreateException
     */
    protected abstract void makeCopyWithDirectory(Path source, Path destination)
            throws CreateException;
}
