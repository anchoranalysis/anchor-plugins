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
import org.anchoranalysis.feature.calc.FeatureCalculationException;
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
    @BeanField @Getter @Setter private int index = 0;
    // END BEAN PROPERTIES

    @Override
    protected double calcFeatureResultFromMoments(ImageMoments moments)
            throws FeatureCalculationException {
        return calcFeatureResultFromSpecificMoment(moments.get(index));
    }

    /** Calculates the result for the specific moment identified by index */
    protected abstract double calcFeatureResultFromSpecificMoment(EigenvalueAndVector moment)
            throws FeatureCalculationException;
}
