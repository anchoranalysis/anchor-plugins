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

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;

/**
 * Calculates the ellipsoidicity of an object by fitting an {@link Ellipsoid} to it.
 *
 * <p>Ellipsoidicity is a measure of how closely the object resembles an ellipsoid. A value of 1.0
 * indicates a perfect ellipsoid, while lower values indicate deviations from an ellipsoidal shape.
 */
public class Ellipsoidicity extends EllipsoidBase {

    @Override
    protected double calculateWithEllipsoid(FeatureInputSingleObject input, Ellipsoid ellipsoid)
            throws FeatureCalculationException {

        return EllipticityCalculatorHelper.calculate(
                input.getObject(), ellipsoid, input.dimensionsRequired());
    }
}
