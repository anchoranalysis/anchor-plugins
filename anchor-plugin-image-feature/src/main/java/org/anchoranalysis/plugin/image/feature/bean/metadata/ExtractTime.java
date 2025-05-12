package org.anchoranalysis.plugin.image.feature.bean.metadata;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * Base class for extracting time information from an image's metadata.
 *
 * <p>It is specified as the number of seconds from the epoch of 1970-01-01T00:00:00Z.
 *
 * <p>If the time is not specified, this feature will return NaN.
 */
public abstract class ExtractTime extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return extractTime(metadata).map(ExtractTime::convertToSeconds).orElse(Double.NaN);
    }

    /**
     * Extracts the time from the given metadata to return as the feature-value (if non-empty).
     *
     * @param metadata to extract a time from.
     * @return the time, or empty if it's not specified
     */
    protected abstract Optional<ZonedDateTime> extractTime(ImageMetadata metadata);

    /** Converts a ZonedDateTime to the number of seconds from the epoch, specified as a double. */
    private static double convertToSeconds(ZonedDateTime dateTime) {
        return (double) dateTime.toEpochSecond();
    }
}
