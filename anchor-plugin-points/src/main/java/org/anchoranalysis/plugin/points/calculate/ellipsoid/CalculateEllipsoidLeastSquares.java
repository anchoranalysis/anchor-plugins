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

package org.anchoranalysis.plugin.points.calculate.ellipsoid;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.ResolvedCalculation;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.plugin.points.calculate.CalculatePointsFromOutline;
import org.anchoranalysis.spatial.point.Point3i;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class CalculateEllipsoidLeastSquares
        extends FeatureCalculation<Ellipsoid, FeatureInputSingleObject> {

    private final boolean suppressZCovariance;
    private final ResolvedCalculation<List<Point3i>, FeatureInputSingleObject> ccPoints;

    public static Ellipsoid of(
            SessionInput<FeatureInputSingleObject> input, boolean suppressZCovariance)
            throws FeatureCalculationException {

        ResolvedCalculation<List<Point3i>, FeatureInputSingleObject> ccPoints =
                input.resolver().search(new CalculatePointsFromOutline());

        ResolvedCalculation<Ellipsoid, FeatureInputSingleObject> ccEllipsoid =
                input.resolver()
                        .search(new CalculateEllipsoidLeastSquares(suppressZCovariance, ccPoints));
        return input.calculate(ccEllipsoid);
    }

    @Override
    protected Ellipsoid execute(FeatureInputSingleObject input) throws FeatureCalculationException {

        try {
            // Shell Rad is arbitrary here for now
            return EllipsoidFactory.createMarkEllipsoidLeastSquares(
                    () -> {
                        try {
                            return ccPoints.getOrCalculate(input);
                        } catch (FeatureCalculationException e) {
                            throw new CreateException(e);
                        }
                    },
                    input.dimensionsRequired(),
                    suppressZCovariance,
                    0.2);
        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
