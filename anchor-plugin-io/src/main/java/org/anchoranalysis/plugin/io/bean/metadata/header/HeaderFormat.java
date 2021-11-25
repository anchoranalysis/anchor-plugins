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
package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;

/**
 * Populates {@link ImageMetadata} from the header of an image-file.
 *
 * <p>The <a href="https://github.com/drewnoakes/metadata-extractor">metadata-extractor</a> from
 * Drew Noakes is used to read the metadata.
 *
 * @author Owen Feehan
 */
public abstract class HeaderFormat extends AnchorBean<HeaderFormat> {

    /**
     * Creates a {@link ImageMetadata}, if possible, from the metadata at {@code path}.
     *
     * @param path the path to an image file.
     * @return the metadata associated with {@code path}, if it was possible to infer it.
     * @throws ImageIOException if the metadata does not meet expectations or I/O fails.
     */
    public Optional<ImageMetadata> populateFrom(Path path) throws ImageIOException {
        try {
            if (format().matches(path)) {
                Metadata metadata =
                        com.drew.imaging.ImageMetadataReader.readMetadata(path.toFile());

                if (metadata == null) {
                    return Optional.empty();
                }

                ImageFileAttributes attributes = ImageFileAttributes.fromPath(path);
                return populateFromMetadata(metadata, attributes);
            } else {
                return Optional.empty();
            }

        } catch (IOException | ImageProcessingException e) {
            throw new ImageIOException("Failed to establish image-metadata", e);
        }
    }

    /**
     * The associated {@link ImageFileFormat} with this header.
     *
     * <p>Only files whose paths end with an extension for this format will be accepted.
     *
     * @return the format.
     */
    protected abstract ImageFileFormat format();

    /**
     * Creates a {@link ImageMetadata}, if possible, from {@code metadata}.
     *
     * @param metadata the {@link Metadata} to infer {@link ImageMetadata} from.
     * @param attributes timestamps and other file-attributes associated with the metadata.
     * @return the inferred metadata, if it was possible to infer it.
     * @throws ImageIOException if the metadata does not meet expectations.
     */
    protected abstract Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes attributes) throws ImageIOException;
}
