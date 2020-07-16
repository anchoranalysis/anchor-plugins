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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.math.moment.ImageMoments;

/**
 * A base class for features that are calculated using image-moments
 *
 * <p>See <a href="Image moment">Image Moment on Wikipedia</a>
 *
 * <p>If there are too few voxels, then a constant value is returned.
 *
 * @author Owen Feehan
 */
public abstract class ImageMomentsBase extends FeatureSingleObject {

    /* Minimum number of voxels, otherwise moments aren't calculated */
    private static final int MIN_NUM_VOXELS = 12;

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private boolean suppressZ = false; // Suppresses covariance in the z-direction.
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        if (input.get().getObject().numPixelsLessThan(MIN_NUM_VOXELS)) {
            return resultIfTooFewPixels();
        }

        return calcFeatureResultFromMoments(calcMoments(input));
    }

    protected abstract double calcFeatureResultFromMoments(ImageMoments moments)
            throws FeatureCalcException;

    protected abstract double resultIfTooFewPixels() throws FeatureCalcException;

    private ImageMoments calcMoments(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalcException {

        ImageMoments moments = input.calc(new CalculateSecondMoments(suppressZ));

        moments = moments.duplicate();

        return moments;
    }
}
