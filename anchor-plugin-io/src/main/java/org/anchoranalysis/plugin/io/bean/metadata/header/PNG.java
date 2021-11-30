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
import com.drew.metadata.png.PngDirectory;
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;
import org.anchoranalysis.spatial.box.Extent;

/**
 * The headers found in a PNG file.
 *
 * @author Owen Feehan
 */
public class PNG extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.PNG;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes attributes) throws ImageIOException {

        Optional<Directory> directory =
                ReadMetadataUtilities.findDirectoryWithName(
                        metadata, PngDirectory.class, "PNG-IHDR");
        if (!directory.isPresent()) {
            return Optional.empty();
        }

        Optional<Extent> extent =
                ReadMetadataUtilities.readFromWidthHeightTags(
                        directory.get(),
                        PngDirectory.TAG_IMAGE_WIDTH,
                        PngDirectory.TAG_IMAGE_HEIGHT);
        if (!extent.isPresent()) {
            return Optional.empty();
        }

        Optional<Integer> numberChannels = numberOfChannels(directory.get());
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }

        Optional<Integer> bitDepth =
                ReadMetadataUtilities.readInt(directory.get(), PngDirectory.TAG_BITS_PER_SAMPLE);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(
                MetadataFactory.createMetadata(
                        extent.get(), numberChannels.get(), bitDepth.get(), attributes));
    }

    private static Optional<Integer> numberOfChannels(Directory directory) throws ImageIOException {
        Optional<Integer> colorType =
                ReadMetadataUtilities.readInt(directory, PngDirectory.TAG_COLOR_TYPE);
        if (!colorType.isPresent()) {
            return Optional.empty();
        }
        switch (colorType.get()) {
            case 0:
                return Optional.of(1); // Grayscale
            case 2:
            case 3:
                return Optional.of(3); // Truecolor or indexed
            case 4:
                return Optional.of(2); // Grayscale and alpha
            case 6:
                return Optional.of(4); // Truecolor and alpha
            default:
                throw new ImageIOException(
                        "Unrecognised color-type in PNG header: " + colorType.get());
        }
    }
}
