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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.functional.CheckedStream;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.io.stack.output.StackWriteOptions;

/**
 * Selects particular writers based on if particular suggested image formats are present.
 *
 * <p>This is useful for mapping a suggested file format by user to the underlying {@link
 * StackWriter} that should write it.
 *
 * @author Owen Feehan
 */
public class BranchSuggestedFormat extends StackWriterDelegateBase {

    // START BEAN PROPERTIES
    /**
     * A list of writers to select if a given format is used
     *
     * <p>The name of the format should be the default extension (without a leading period) of the
     * corresponding format. Case is irrelevant.
     */
    @BeanField @Getter @Setter private List<StackWriter> writersIfSuggested;

    /** The writer to use if there is no suggested file-format. */
    @BeanField @Getter @Setter private StackWriter writerIfNoSuggestion;
    // END BEAN PROPERTIES

    @Override
    protected StackWriter selectDelegate(StackWriteOptions writeOptions) throws ImageIOException {

        Optional<ImageFileFormat> format = writeOptions.getSuggestedFormatToWrite();
        if (format.isPresent()) {
            String extension = format.get().getDefaultExtension();
            return writerForExtension(extension, writeOptions)
                    .orElseThrow(
                            () ->
                                    new ImageIOException(
                                            "No stack-writer is present in 'writers' that matches "
                                                    + extension));
        } else {
            return writerIfNoSuggestion;
        }
    }

    /**
     * Searches for an extension in {@code writers} whose default-extension matches {@code
     * extension}.
     *
     * @throws ImageIOException
     */
    private Optional<StackWriter> writerForExtension(
            String extension, StackWriteOptions writeOptions) throws ImageIOException {
        return CheckedStream.filter(
                        writersIfSuggested.stream(),
                        ImageIOException.class,
                        writer -> writer.fileFormat(writeOptions).matchesIdentifier(extension))
                .findFirst();
    }
}
