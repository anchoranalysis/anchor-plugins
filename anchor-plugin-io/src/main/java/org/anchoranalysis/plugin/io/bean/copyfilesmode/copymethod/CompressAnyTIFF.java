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

package org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod;

import java.nio.file.Path;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.format.ImageFileFormat;

/**
 * Create a voxelwise <b>compressed copy</b> of any TIFF file being copied, and otherwise do a
 * {@link Bytewise} copy.
 *
 * @author Owen Feehan
 */
public class CompressAnyTIFF extends CopyFilesMethod {

    private Bytewise simpleCopy = new Bytewise();

    @Override
    public void makeCopyWithDirectory(Path source, Path destination) throws CreateException {

        String fileName = source.getFileName().toString().toLowerCase();

        try {
            if (ImageFileFormat.TIFF.matchesEnd(fileName)) {
                CopyTIFFAndCompress.apply(source.toString(), destination);
            } else {
                simpleCopy.makeCopy(source, destination);
            }
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
