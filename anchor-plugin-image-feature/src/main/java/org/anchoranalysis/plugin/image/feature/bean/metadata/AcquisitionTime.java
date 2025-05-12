package org.anchoranalysis.plugin.image.feature.bean.metadata;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.anchoranalysis.image.core.stack.ImageMetadata;

/**
 * The acquisition time of the image, if specified in the metadata.
 *
 * <p>It is specified as the number of seconds from the epoch of 1970-01-01T00:00:00Z.
 *
 * <p>If the acquisition time is not specified, this feature will return NaN.
 */
public class AcquisitionTime extends ExtractTime {

    @Override
    protected Optional<ZonedDateTime> extractTime(ImageMetadata metadata) {
        return metadata.getAcquisitionTime();
    }
}
