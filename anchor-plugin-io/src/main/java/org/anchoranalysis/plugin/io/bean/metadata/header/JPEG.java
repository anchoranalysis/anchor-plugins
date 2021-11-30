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
import com.drew.metadata.jpeg.JpegDirectory;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.AcquisitionDateReader;
import org.anchoranalysis.io.bioformats.metadata.OrientationReader;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;

/**
 * The headers found in a JPEG file.
 *
 * @author Owen Feehan
 */
public class JPEG extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.JPEG;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes attributes) throws ImageIOException {
        Optional<OrientationChange> orientation =
                OrientationReader.determineOrientationCorrection(metadata);

        // Infer width and height from the metadata.
        // Image resolution is ignored.
        Optional<Dimensions> dimensions =
                FromExifIfPossible.inferExtentFromEXIFOr(metadata, orientation)
                        .map(Dimensions::new);

        if (dimensions.isPresent()) {
            return inferRemainingAttributes(metadata, dimensions.get(), attributes);
        }

        return Optional.empty();
    }

    /** Infers the remaining needed attributes, once the dimensions are known. */
    private static Optional<ImageMetadata> inferRemainingAttributes(
            Metadata metadata, Dimensions dimensions, ImageFileAttributes timestamps) {

        // Then infer the number of channels.
        Optional<Integer> numberChannels = inferNumberChannels(metadata);
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }

        // Then infer the bit-depth
        Optional<Integer> bitDepth = inferBitDepth(metadata);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }

        Optional<ZonedDateTime> acqusitionDate =
                AcquisitionDateReader.readAcquisitionDate(metadata);

        // Assume any image with three channels is RGB encoded.
        boolean rgb = numberChannels.get() == 3;
        return Optional.of(
                new ImageMetadata(
                        dimensions,
                        numberChannels.get(),
                        1,
                        rgb,
                        bitDepth.get(),
                        timestamps,
                        acqusitionDate)); // NOSONAR
    }

    /**
     * Infers the <b>number of channels</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the number of channels.
     */
    private static Optional<Integer> inferNumberChannels(Metadata metadata) {
        return ReadMetadataUtilities.readInt(
                metadata, JpegDirectory.class, JpegDirectory.TAG_NUMBER_OF_COMPONENTS);
    }

    /**
     * Infers the <b>bit depth</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the bit depth.
     */
    private static Optional<Integer> inferBitDepth(Metadata metadata) {
        return ReadMetadataUtilities.readInt(
                metadata, JpegDirectory.class, JpegDirectory.TAG_DATA_PRECISION);
    }
}
