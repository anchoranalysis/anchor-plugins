package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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
 *   <li>Original photo-taken time from EXIF metadata if available, and the file has a jpg or jpeg
 *       extension.</i>
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
     * @throws CreateException if an IO error occurs accessing the file.
     */
    public TimestampedFile(
            File file,
            VarianceCalculatorDouble varianceCreationTime,
            List<TimestampPattern> filenamePatterns)
            throws CreateException {
        try {
            this.file = file;
            this.filenamePatterns = filenamePatterns;
            this.timestamp = associateDateTime(file);
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
    private long associateDateTime(File file) throws IOException {

        if (ImageFileFormat.JPEG.matchesEnd(file.getName())) {
            try {
                return readEXIFCreationTime(file);
            } catch (ServiceException | IOException e) {
                // Ignore any errors occur reading the EXIF and continue
            }
        }

        Optional<Long> extractedTime = extractTimeFromFilename(file.getName());

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
    private Optional<Long> extractTimeFromFilename(String fileName) {
        Stream<Optional<Long>> extractedDateTime =
                filenamePatterns.stream().map(pattern -> pattern.match(fileName));
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
    private static long readEXIFCreationTime(File file) throws ServiceException, IOException {
        EXIFServiceImpl exif = new EXIFServiceImpl();
        exif.initialize(file.toString());
        return exif.getCreationDate().toInstant().getEpochSecond();
    }
}
