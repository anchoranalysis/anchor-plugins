/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.feature.calc.FeatureCalculationException;
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
            throws FeatureCalculationException {

        moments.removeClosestToUnitZ();

        double moments0 = moments.get(0).getEigenvalue();
        double moments1 = moments.get(1).getEigenvalue();

        if (moments0 == 0.0 && moments1 == 0.0) {
            return 1.0;
        }

        if (moments0 == 0.0 || moments1 == 0.0) {
            throw new FeatureCalculationException("All moments are 0");
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
