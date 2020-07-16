/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.EigenvalueAndVector;
import org.anchoranalysis.math.moment.ImageMoments;

/**
 * A feature based one one-specific principal-axis as identified by Image Moments.
 *
 * <p>See <a href="Image moment">Image Moment on Wikipedia</a>
 *
 * <p>Principal axes are ordered by eigen-value, with 0 being the largest, 1 being the
 * second-largest etc..
 *
 * @author Owen Feehan
 */
public abstract class SpecificAxisBase extends ImageMomentsBase {

    // START BEAN PROPERTIES
    /**
     * Specifies which principal-axis to use for calculations (0=largest eigenvalue,
     * 1=second-largest eigenvalue etc.)
     */
    @BeanField private int index = 0;
    // END BEAN PROPERTIES

    @Override
    protected double calcFeatureResultFromMoments(ImageMoments moments)
            throws FeatureCalcException {
        return calcFeatureResultFromSpecificMoment(moments.get(index));
    }

    /** Calculates the result for the specific moment identified by index */
    protected abstract double calcFeatureResultFromSpecificMoment(EigenvalueAndVector moment)
            throws FeatureCalcException;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
