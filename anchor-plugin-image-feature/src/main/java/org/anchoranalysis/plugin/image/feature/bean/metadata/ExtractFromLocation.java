package org.anchoranalysis.plugin.image.feature.bean.metadata;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageLocation;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * Extracts a double value from a {@link ImageLocation} if it exists in a {@link ImageMetadata}.
 *
 * <p>If the location is unknown, then Double.NaN is returned.
 */
public abstract class ExtractFromLocation extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return metadata.getLocation().map(this::extractValue).orElse(Double.NaN);
    }

    /**
     * Extracts the value that is returned from the feature if an image-location is present.
     *
     * @param location the location to extract the value from.
     * @return the value to be returned.
     */
    protected abstract double extractValue(ImageLocation location);
}
