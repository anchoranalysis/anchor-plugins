package org.anchoranalysis.plugin.image.feature.bean.metadata;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * The number of channels in an image.
 *
 * <p>e.g. 3 for RGB or 1 for grayscale.
 *
 * @author Owen Feehan
 */
public class NumberChannels extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return metadata.getNumberChannels();
    }
}
