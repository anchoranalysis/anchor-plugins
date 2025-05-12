/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2025 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import loci.common.services.ServiceException;
import loci.formats.services.EXIFServiceImpl;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.plugin.io.bean.file.pattern.TimestampPattern;

/** Associates a date-time with a file based on various criteria. */
@RequiredArgsConstructor
class DateTimeAssociator {

    /** Patterns used to extract timestamps from filenames. */
    private final List<TimestampPattern> filenamePatterns;

    /**
     * Finds a date-time to associate with the file.
     *
     * <p>If there's a JPEG extension, a creation-time is read from EXIF metadata, if it exists.
     *
     * <p>Then preference is given to a date-time that can be extracted from the filename, falling
     * backing to file creation-time.
     *
     * @param file the {@link File} to associate a date-time with.
     * @param offset the {@link ZoneOffset} to use for the date-time.
     * @return the associated date-time as seconds since the epoch.
     * @throws IOException if an I/O error occurs while reading file attributes.
     */
    public long associateDateTime(File file, ZoneOffset offset) throws IOException {
        if (ImageFileFormat.JPEG.matches(file.getName())) {
            Optional<Long> exifTimestamp = readEXIFCreationTime(file);
            if (exifTimestamp.isPresent()) {
                return exifTimestamp.get();
            }
        }

        Optional<Long> extractedTime = extractTimeFromFilename(file.getName(), offset);

        if (extractedTime.isPresent()) {
            return extractedTime.get();
        }

        return readFileCreationTime(file);
    }

    /**
     * Extracts a timestamp from a filename using the defined patterns.
     *
     * @param fileName the name of the file to extract the timestamp from.
     * @param offset the {@link ZoneOffset} to use for the extracted timestamp.
     * @return an {@link Optional} containing the extracted timestamp, or empty if no timestamp
     *     could be extracted.
     */
    private Optional<Long> extractTimeFromFilename(String fileName, ZoneOffset offset) {
        Stream<Optional<Long>> extractedDateTime =
                filenamePatterns.stream().map(pattern -> pattern.match(fileName, offset));
        return OptionalUtilities.orFlat(extractedDateTime);
    }

    /**
     * Reads the creation time of a file.
     *
     * @param file the {@link File} to read the creation time from.
     * @return the creation time as seconds since the epoch.
     * @throws IOException if an I/O error occurs while reading file attributes.
     */
    private static long readFileCreationTime(File file) throws IOException {
        BasicFileAttributes attributes =
                Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return attributes.creationTime().toMillis() / 1000;
    }

    /**
     * Reads the creation time from the EXIF metadata of a JPEG file.
     *
     * @param file the {@link File} to read the EXIF metadata from.
     * @return an {@link Optional} containing the EXIF creation time as seconds since the epoch, or
     *     empty if no EXIF creation time could be read.
     */
    private static Optional<Long> readEXIFCreationTime(File file) {
        EXIFServiceImpl exif = new EXIFServiceImpl();
        try {
            exif.initialize(file.toString());
        } catch (ServiceException | IOException e) {
            return Optional.empty();
        }

        if (exif.getCreationDate() != null) {
            return Optional.of(exif.getCreationDate().toInstant().getEpochSecond());
        } else {
            return Optional.empty();
        }
    }
}
