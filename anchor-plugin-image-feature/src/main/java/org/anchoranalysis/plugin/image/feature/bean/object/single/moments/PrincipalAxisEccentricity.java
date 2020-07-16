/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.ImageMoments;

/**
 * Calculates the eccentricity of the Principal Axes (as defined by Image Moments)
 *
 * <p>Specifically, this is
 *
 * <pre>sqrt( 1 - eigenvalue2/eigenvalue1)</pre>
 *
 * where <code>eigenvalue1</code> is the eigen-value with first highest-magnitude, and <code>
 * eigenvalue2</code> is second-highest etc..
 *
 * <p>See <a href="https://en.wikipedia.org/wiki/Image_moment">Image moment on Wikipedia</a> for the
 * precise calculation.</a>
 *
 * @author Owen Feehan
 */
public class PrincipalAxisEccentricity extends ImageMomentsBase {

    @Override
    protected double calcFeatureResultFromMoments(ImageMoments moments)
            throws FeatureCalcException {

        moments.removeClosestToUnitZ();

        double moments0 = moments.get(0).getEigenvalue();
        double moments1 = moments.get(1).getEigenvalue();

        if (moments0 == 0.0 && moments1 == 0.0) {
            return 1.0;
        }

        if (moments0 == 0.0 || moments1 == 0.0) {
            throw new FeatureCalcException("All moments are 0");
        }

        double eccentricity = calcEccentricity(moments1, moments0);
        assert (!Double.isNaN(eccentricity));
        return eccentricity;
    }

    public static double calcEccentricity(double eigenvalSmaller, double eigenvalLarger) {
        return Math.sqrt(1.0 - eigenvalSmaller / eigenvalLarger);
    }

    @Override
    protected double resultIfTooFewPixels() {
        return 1.0;
    }
}
