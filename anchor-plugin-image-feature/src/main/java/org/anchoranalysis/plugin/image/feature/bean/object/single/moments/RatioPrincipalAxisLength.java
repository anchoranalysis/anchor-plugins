/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.ImageMoments;

/**
 * Calculates the ratio of prinicpal-axis length using Image Moments
 *
 * <p>Specifically this is the highest-magnitude eigen-value (normalized) to second-highest
 * (normalized) eigen-value.
 *
 * <p>See <a href="https://en.wikipedia.org/wiki/Image_moment">Image moment on Wikipedia</a> for the
 * precise calculation.</a>
 *
 * <p>See <a
 * href="http://stackoverflow.com/questions/1711784/computing-object-statistics-from-the-second-central-moments">
 * Stack overflow post</a> for the normalization procedure.</a>
 *
 * @author Owen Feehan
 */
public class RatioPrincipalAxisLength extends ImageMomentsBase {

    @Override
    protected double calcFeatureResultFromMoments(ImageMoments moments)
            throws FeatureCalcException {

        moments.removeClosestToUnitZ();

        double moments0 = moments.get(0).eigenvalueNormalizedAsAxisLength();
        double moments1 = moments.get(1).eigenvalueNormalizedAsAxisLength();

        if (moments0 == 0.0 && moments1 == 0.0) {
            return 1.0;
        }

        if (moments0 == 0.0 || moments1 == 0.0) {
            throw new FeatureCalcException("All moments are 0");
        }

        return moments0 / moments1;
    }

    @Override
    protected double resultIfTooFewPixels() {
        return 1.0;
    }
}
