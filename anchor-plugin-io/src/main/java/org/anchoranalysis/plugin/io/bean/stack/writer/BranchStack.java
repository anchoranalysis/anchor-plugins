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
package org.anchoranalysis.plugin.io.bean.stack.writer;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.io.stack.output.StackWriteOptions;

/**
 * Uses different raster-writers depending on whether it is always 2D (not a z-stack) or possibly
 * 3D.
 *
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case.
 *
 * @author Owen Feehan
 */
public class BranchStack extends StackWriterDelegateBase {

    // START BEAN PROPERTIES
    /** Writer to use if it is guaranteed that the image will always be 2D. */
    @BeanField @Getter @Setter private StackWriter writerAlways2D;

    /** Otherwise the writer to use. */
    @BeanField @Getter @Setter private StackWriter writerElse;

    // END BEAN PROPERTIES

    @Override
    protected StackWriter selectDelegate(StackWriteOptions writeOptions) {
        if (writeOptions.getAttributes().isAlways2D()) {
            return writerAlways2D;
        } else {
            return writerElse;
        }
    }
}
