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
import lombok.Getter;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.math.statistics.MeanScale;
import org.anchoranalysis.math.statistics.VarianceCalculatorDouble;
import org.anchoranalysis.plugin.io.bean.file.pattern.TimestampPattern;
import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * A file with an associated date-time timestamp, exposed so that it can be clustered by this
 * timestamp.
 *
 * <p>The timestamp is chosen, in this order of priority:
 *
 * <ul>
 *   <li>A date / time string extracted from the filename, if exists in particular patterns, falling
 *       back to creation-time, if none exists.
 *   <li>Original photo-taken time from EXIF metadata if available, and the file has a <i>jpg</i> or
 *       <i>jpeg extension.</i>
 *   <li><i>File creation time.</i>
 * </ul>
 *
 * <p>Timezones are assumed to be the current time-zone, if not otherwise indicated.
 *
 * @author Owen Feehan
 */
class TimestampedFile implements Clusterable {

    /** Patterns that can extract a timestamp from a filename. */
    private final List<TimestampPattern> filenamePatterns;

    /** The file whose attributes will be used to cluster. */
    @Getter private final File file;

    /** The associated timestamp with the file, stored as seconds from the epoch. */
    @Getter private long timestamp;

    /**
     * A normalzed version of {@code timestamp} after standard-deviation has been calculated for the
     * entire population.
     */
    private double normalizedTimestamp;

    /**
     * Creates for a particular file.
     *
     * @param file the file.
     * @param varianceCreationTime a running-sum style calculator for variance, to which the
     *     date-time is added.
     * @param filenamePatterns patterns that can extract a date-time from a filename.
     * @param offset the offset to assume the time-stamp belongs in.
     * @throws CreateException if an IO error occurs accessing the file.
     */
    public TimestampedFile(
            File file,
            VarianceCalculatorDouble varianceCreationTime,
            List<TimestampPattern> filenamePatterns,
            ZoneOffset offset)
            throws CreateException {
        try {
            this.file = file;
            this.filenamePatterns = filenamePatterns;
            this.timestamp = associateDateTime(file, offset);
            varianceCreationTime.add(timestamp);
        } catch (IOException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Finds a date-time to associate with the file.
     *
     * <p>Preference is given to a date-time that can be extracted from the filename, falling
     * backing to file creation-time.
     */
    private long associateDateTime(File file, ZoneOffset offset) throws IOException {

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
     * Normalizes the date-time, after a mean and scale is given for the entire population.
     *
     * @param meanScaleDateTime mean and scale parameters for the date-time population.
     */
    public void normalize(MeanScale meanScaleDateTime) {
        this.normalizedTimestamp = meanScaleDateTime.zScore(timestamp);
    }

    @Override
    public double[] getPoint() {
        return new double[] {normalizedTimestamp};
    }

    /** Matches certain times. */
    private Optional<Long> extractTimeFromFilename(String fileName, ZoneOffset offset) {
        Stream<Optional<Long>> extractedDateTime =
                filenamePatterns.stream().map(pattern -> pattern.match(fileName, offset));
        return OptionalUtilities.orFlat(extractedDateTime);
    }

    /** Reads the file-creation time. */
    private static long readFileCreationTime(File file) throws IOException {
        BasicFileAttributes attributes =
                Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // The creation time converted to seconds
        return attributes.creationTime().toMillis() / 1000;
    }

    /** Reads the creation-time from an EXIF header if it is available. */
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
