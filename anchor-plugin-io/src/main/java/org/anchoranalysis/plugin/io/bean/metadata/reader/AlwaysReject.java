/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.metadata.reader;

import java.nio.file.Path;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;

/**
 * A special {@link ImageMetadataReader} that will always throw an exception and reject a file.
 *
 * <p>This can be useful for debugging errors, to identify circumstances in which a {@link
 * ImageMetadataReader} is used or not used.
 *
 * @author Owen Feehan
 */
public class AlwaysReject extends ImageMetadataReader {

    @Override
    public ImageMetadata openFile(
            Path path, StackReader defaultStackReader, OperationContext context)
            throws ImageIOException {
        throw new ImageIOException(
                "This image has been rejected by the ImageMetadataReader by design.");
    }
}
