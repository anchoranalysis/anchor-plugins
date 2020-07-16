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
