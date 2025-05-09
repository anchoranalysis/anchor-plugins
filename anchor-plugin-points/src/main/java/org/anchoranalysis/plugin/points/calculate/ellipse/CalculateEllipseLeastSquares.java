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

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.mpp.mark.conic.Ellipse;
import org.anchoranalysis.plugin.points.bean.fitter.LinearLeastSquaresEllipseFitter;

/**
 * Calculates the best-fit ellipse for an {@link ObjectMask} using linear least squares.
 *
 * <p>This class fits an {@link Ellipse} to the center of gravity slice of the input object.
 */
@EqualsAndHashCode(callSuper = false)
public class CalculateEllipseLeastSquares
        extends CalculationPart<ObjectWithEllipse, FeatureInputSingleObject> {

    /** Factory for creating ellipses using the linear least squares method. */
    @EqualsAndHashCode.Exclude private EllipseFactory factory;

    /** Creates a new instance of {@link CalculateEllipseLeastSquares}. */
    public CalculateEllipseLeastSquares() {
        factory = new EllipseFactory(new LinearLeastSquaresEllipseFitter());
    }

    @Override
    protected ObjectWithEllipse execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        try {
            EnergyStackWithoutParameters energyStack =
                    input.getEnergyStackRequired().withoutParameters();

            ObjectMask object = extractEllipseSlice(input.getObject());

            // Shell Rad is arbitrary here for now
            Ellipse mark = factory.create(object, energyStack.dimensions(), 0.2);

            return new ObjectWithEllipse(object, mark);
        } catch (CreateException | InsufficientPointsException e) {
            throw new FeatureCalculationException(e);
        }
    }

    /**
     * Extracts the center of gravity slice from a 3D {@link ObjectMask}.
     *
     * @param object the 3D object mask
     * @return a 2D object mask representing the center of gravity slice
     */
    private static ObjectMask extractEllipseSlice(ObjectMask object) {
        int zSliceCenter = (int) object.centerOfGravity().z();
        return object.extractSlice(zSliceCenter, false);
    }
}
