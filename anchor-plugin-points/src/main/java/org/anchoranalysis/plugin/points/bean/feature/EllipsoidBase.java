/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.bean.feature;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.CalculateEllipsoidLeastSquares;

/**
 * Base class for features that calculate properties of an {@link Ellipsoid} fitted to an object.
 */
public abstract class EllipsoidBase extends FeatureSingleObject {

    /** If fewer voxels exist in an object than this, it is assumed to be perfectly ellipsoidal */
    private static final int MINIMUM_NUMBER_VOXELS = 12;

    // START BEAN PROPERTIES
    /** If true, suppresses covariance in the z-direction. */
    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        ObjectMask object = input.get().getObject();

        // If we have these few pixels, assume we are perfectly ellipsoid
        if (object.voxelsOn().lowerCountExistsThan(MINIMUM_NUMBER_VOXELS)) {
            return 1.0;
        }

        Ellipsoid ellipsoid = CalculateEllipsoidLeastSquares.of(input, suppressZ);

        return calculateWithEllipsoid(input.get(), ellipsoid);
    }

    /**
     * Calculates a feature value based on the fitted {@link Ellipsoid}.
     *
     * @param input the input object
     * @param ellipsoid the fitted ellipsoid
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateWithEllipsoid(
            FeatureInputSingleObject input, Ellipsoid ellipsoid) throws FeatureCalculationException;
}
