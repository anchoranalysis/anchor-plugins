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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.plugin.io.bean.metadata.header.HeaderFormat;
import org.anchoranalysis.plugin.io.bean.metadata.header.JPEG;

/**
 * Tries to construct the {@link ImageMetadata} from EXIF and other metadata, if available, or
 * otherwise falls back to another reader.
 *
 * <p>It supports a limited number of file-types, as identified by an extension in the path. By
 * default, it supports:
 *
 * <ul>
 *   <li>JPEG (.jpg or .jpeg)
 *   <li>PNG (.png)
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class InferFromHeader extends ImageMetadataReader {

    // START BEAN PROPERTIES
    /** Fallback to use if EXIF information is non-existing or absent. */
    @BeanField @Getter @Setter private ImageMetadataReader fallback;

    /**
     * The formats whose headers will be searched, to find sufficient metadata to populate {@link
     * ImageMetadata}.
     */
    @BeanField @Getter @Setter private List<HeaderFormat> formats = createDefaultFormats();

    // END BEAN PROPERTIES

    @Override
    public ImageMetadata openFile(
            Path path, StackReader defaultStackReader, OperationContext context)
            throws ImageIOException {

        return OptionalUtilities.orElseGet(
                attemptToPopulateFromMetadata(path, context.getLogger().errorReporter()),
                () -> useFallbackReader(path, defaultStackReader, context));
    }

    /**
     * Try to infer the needed elements for {@link ImageMetadata} from metadata present in {@code
     * path}.
     */
    private Optional<ImageMetadata> attemptToPopulateFromMetadata(
            Path path, ErrorReporter errorReporter) throws ImageIOException {
        for (HeaderFormat format : formats) {
            try {
                Optional<ImageMetadata> metadata = format.populateFrom(path);
                if (metadata.isPresent()) {
                    return Optional.of(metadata.get());
                }
            } catch (Exception e) {
                errorReporter.recordError(
                        InferFromHeader.class,
                        "Switching to fallback, as cannot infer metadata from the header due to the following exception.",
                        e);
            }
        }
        return Optional.empty();
    }

    /** Use the fallback {@code ImageMetadataReader} to establish the metadata. */
    private ImageMetadata useFallbackReader(
            Path path, StackReader defaultStackReader, OperationContext context)
            throws ImageIOException {
        return fallback.openFile(path, defaultStackReader, context);
    }

    private static List<HeaderFormat> createDefaultFormats() {
        return Arrays.asList(new JPEG());
    }
}
