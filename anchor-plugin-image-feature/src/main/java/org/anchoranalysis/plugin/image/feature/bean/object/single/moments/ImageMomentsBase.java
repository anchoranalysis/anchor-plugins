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
