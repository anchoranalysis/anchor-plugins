package org.anchoranalysis.plugin.image.feature.bean.metadata;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * The bit-depth of the image.
 *
 * <p>e.g. 8-bit for byte versus 16-bit for short etc.
 *
 * @author Owen Feehan
 */
public class BitDepth extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return metadata.getBitDepth();
    }
}
