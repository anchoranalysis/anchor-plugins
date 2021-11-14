package org.anchoranalysis.plugin.image.feature.bean.metadata;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.bean.FeatureImageMetadata;

/**
 * The number of frames in an image.
 *
 * <p>Each frame is an images across the time-dimension.
 *
 * @author Owen Feehan
 */
public class NumberFrames extends FeatureImageMetadata {

    @Override
    public double calculate(ImageMetadata metadata) throws FeatureCalculationException {
        return metadata.getNumberFrames();
    }
}
