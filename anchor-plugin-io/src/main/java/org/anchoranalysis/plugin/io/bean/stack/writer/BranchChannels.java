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
import java.util.function.Supplier;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.io.stack.output.StackWriteAttributes;
import org.anchoranalysis.image.io.stack.output.StackWriteOptions;

/**
 * Uses different raster-writers depending on the number/type of channels.
 *
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case. An
 * exception is {@code whenBinaryChannel}, which instead falls back to {@code whenSingleChannel} if unspecified.
 *
 * <p>{@code whenBinaryChannel} is given precedence over {@code whenSingleChannel}.
 * 
 * @author Owen Feehan
 */
public class BranchChannels extends StackWriterDelegateBase {

    // START BEAN PROPERTIES
    /** Default writer, if a more specific writer is not specified for a condition. */
    @BeanField @Getter @Setter private StackWriter writer;

    /**
     * Writer employed if a stack is a single-channeled image, not guaranteed to be binary.
     */
    @BeanField @OptionalBean @Getter @Setter private StackWriter whenSingleChannel;
    
    /**
     * Writer employed if a stack is a <b>three-channeled non-RGB</b> image.
     */
    @BeanField @OptionalBean @Getter @Setter private StackWriter whenThreeChannels;

    /** 
     * Writer employed if a stack is a <b>three-channeled RGB</b> image.
     */
    @BeanField @OptionalBean @Getter @Setter private StackWriter whenRGB;
    
    /** 
     * Writer employed if a stack is a <b>single-channeled binary</b> image.
     */
    @BeanField @OptionalBean @Getter @Setter private StackWriter whenBinaryChannel;
    // END BEAN PROPERTIES

    @Override
    protected StackWriter selectDelegate(StackWriteOptions writeOptions) {
        StackWriteAttributes attributes = writeOptions.getAttributes();
        if (attributes.isRgb()) {
            return writerOrDefault(whenRGB);
        } else if (attributes.isThreeChannels()) {
            return writerOrDefault(whenThreeChannels);
        } else if (attributes.isSingleChannel()) {
            return singleChannel(writeOptions);
        } else {
            return writer;
        }
    }
    
    private StackWriter singleChannel(StackWriteOptions writeOptions) {
        if (writeOptions.getAttributes().isBinary()) {
            return writerOrFallback(whenBinaryChannel, this::singleNonBinaryChannel);
        } else {
            return singleNonBinaryChannel();
        }
    }
    
    private StackWriter singleNonBinaryChannel() {
        return writerOrDefault(whenSingleChannel);
    }

    private StackWriter writerOrDefault(StackWriter writerMaybeNull) {
        return writerOrFallback(writerMaybeNull, () -> writer);
    }
    
    private static StackWriter writerOrFallback(StackWriter writerMaybeNull, Supplier<StackWriter> writerFallback) {
        if (writerMaybeNull != null) {
            return writerMaybeNull;
        } else {
            return writerFallback.get();
        }
    }
}
