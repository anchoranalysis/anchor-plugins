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

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;
import org.anchoranalysis.spatial.box.Extent;

/**
 * The headers found in a BMP file.
 *
 * @author Owen Feehan
 */
public class BMP extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.BMP;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes attributes) throws ImageIOException {

        Directory directory = metadata.getFirstDirectoryOfType(BmpHeaderDirectory.class);
        if (directory == null) {
            return Optional.empty();
        }

        Optional<Extent> extent =
                ReadMetadataUtilities.readFromWidthHeightTags(
                        directory,
                        BmpHeaderDirectory.TAG_IMAGE_WIDTH,
                        BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
        if (!extent.isPresent()) {
            return Optional.empty();
        }

        Optional<Integer> bitDepth =
                ReadMetadataUtilities.readInt(directory, BmpHeaderDirectory.TAG_BITS_PER_PIXEL);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        switch (bitDepth.get()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return createMetadata(extent.get(), 1, 8, attributes);
            case 16:
                return createMetadata(extent.get(), 2, 8, attributes);
            case 24:
                return createMetadata(extent.get(), 3, 8, attributes);
            case 32:
                return createMetadata(extent.get(), 4, 8, attributes);
            default:
                throw new ImageIOException(
                        String.format("Unrecognised bitsPerPixel of %d", bitDepth.get()));
        }
    }

    private static Optional<ImageMetadata> createMetadata(
            Extent extent, int numberChannels, int pixelDepth, ImageFileAttributes timestamps) {
        return Optional.of(
                MetadataFactory.createMetadata(extent, numberChannels, pixelDepth, timestamps));
    }
}
