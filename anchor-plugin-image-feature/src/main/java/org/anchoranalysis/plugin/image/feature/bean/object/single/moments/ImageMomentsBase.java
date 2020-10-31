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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.math.statistics.moment.ImageMoments;

/**
 * A base class for features that are calculated using image-moments
 *
 * <p>If there are too few voxels, then a constant value is returned.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Image_moment">Image Moment on Wikipedia</a>
 * @author Owen Feehan
 */
public abstract class ImageMomentsBase extends FeatureSingleObject {

    /* Minimum number of voxels, otherwise moments aren't calculated */
    private static final int MIN_NUM_VOXELS = 12;

    // START BEAN PROPERTIES
    /** If true co-voariance is suprpessed in z-dimension */
    @BeanField @Getter @Setter private boolean suppressZ = false;

    /**
     * A value to return if there are too few voxels (less than {@code MIN_NUM_VOXELS} to calculate
     * moments
     *
     * <p>A warning message is also written to the log.
     */
    @BeanField @Getter @Setter private double valueIfTooFewVoxels = Double.NaN;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        if (input.get().getObject().voxelsOn().lowerCountExistsThan(MIN_NUM_VOXELS)) {
            String errorMessage = errorMessageIfTooFewPixels();

            getLogger().errorReporter().recordError(ImageMomentsBase.class, errorMessage);

            return valueIfTooFewVoxels;
        }

        return calculateFromAllMoments(calculateMoments(input));
    }

    protected abstract double calculateFromAllMoments(ImageMoments moments)
            throws FeatureCalculationException;

    protected abstract String errorMessageIfTooFewPixels() throws FeatureCalculationException;

    private ImageMoments calculateMoments(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        ImageMoments moments = input.calculate(new CalculateSecondMoments(suppressZ));

        moments = moments.duplicate();

        return moments;
    }
}
