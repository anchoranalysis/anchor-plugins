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
/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.points.calculate.ellipse.CalculateEllipseLeastSquares;
import org.anchoranalysis.plugin.points.calculate.ellipse.ObjectWithEllipse;

// Calculates the ellipticity of an object-mask (on the COG slice if it's a zstack)
public class Ellipticity extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        FeatureInputSingleObject inputSessionless = input.get();

        ObjectWithEllipse both;
        try {
            both = input.calc(new CalculateEllipseLeastSquares());
        } catch (FeatureCalcException e) {
            if (e.getCause() instanceof InsufficientPointsException) {
                // If we don't have enough points, we return perfectly ellipticity as it's so small
                return 1.0;
            } else {
                throw new FeatureCalcException(e);
            }
        }

        ObjectMask object = both.getObject();

        // If we have these few pixels, assume we are perfectly ellipsoid
        if (object.numPixelsLessThan(6)) {
            return 1.0;
        }

        return EllipticityCalculatorHelper.calc(
                object, both.getMark(), inputSessionless.getDimensionsRequired());
    }
}
