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

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Infers the size of the image from tags in {@link Metadata}.
 *
 * <p>It tries first to read from any EXIF header, and if that is absent, then a fallback.
 *
 * <p>All are presumes to describe the image <b><i>before</i></b> any {@link OrientationChange} is
 * applied.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FromExifIfPossible {

    /**
     * Infers the {@link Extent} from the metadata.
     *
     * @param metadata the metadata.
     * @param orientation manipulation to apply to any width/height in {@code metadata} after it is
     *     read.
     * @return the extent, if it is existed, orientated to match {@code orientation}.
     * @throws ImageIOException if metadata exists in an invalid state.
     */
    public static Optional<Extent> inferExtentFromEXIFOr(
            Metadata metadata, Optional<OrientationChange> orientation) throws ImageIOException {

        Optional<Extent> extent =
                OptionalUtilities.orFlatSupplier(
                        () -> readExif(metadata), () -> readOther(metadata));

        if (extent.isPresent()) {
            if (orientation.isPresent()) {
                return Optional.of(orientation.get().extent(extent.get()));
            } else {
                return extent;
            }
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Extent> readExif(Metadata metadata) throws ImageIOException {
        return ReadMetadataUtilities.readFromWidthHeightTags(
                metadata,
                ExifIFD0Directory.class,
                ExifDirectoryBase.TAG_IMAGE_WIDTH,
                ExifDirectoryBase.TAG_IMAGE_HEIGHT);
    }

    private static Optional<Extent> readOther(Metadata metadata) throws ImageIOException {
        return ReadMetadataUtilities.readFromWidthHeightTags(
                metadata,
                JpegDirectory.class,
                JpegDirectory.TAG_IMAGE_WIDTH,
                JpegDirectory.TAG_IMAGE_HEIGHT);
    }
}
