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

package org.anchoranalysis.plugin.points.calculate.ellipse;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipseFitter;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@EqualsAndHashCode(callSuper = false)
public class CalculateEllipseLeastSquares
        extends FeatureCalculation<ObjectWithEllipse, FeatureInputSingleObject> {

    @EqualsAndHashCode.Exclude private EllipseFactory factory;

    public CalculateEllipseLeastSquares() {
        factory = new EllipseFactory(new LinearLeastSquaresEllipseFitter());
    }

    @Override
    protected ObjectWithEllipse execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        try {
            NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();

            ObjectMask object = extractEllipseSlice(input.getObject());

            // Shell Rad is arbitrary here for now
            MarkEllipse mark = factory.create(object, nrgStack.dimensions(), 0.2);

            return new ObjectWithEllipse(object, mark);
        } catch (CreateException | InsufficientPointsException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private static ObjectMask extractEllipseSlice(ObjectMask object) {
        int zSliceCenter = (int) object.centerOfGravity().z();
        return object.extractSlice(zSliceCenter, false);
    }
}
