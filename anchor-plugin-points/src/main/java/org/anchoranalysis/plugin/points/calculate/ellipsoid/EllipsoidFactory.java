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
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.function.CheckedSupplier;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;
import org.anchoranalysis.plugin.points.bean.fitter.LinearLeastSquaresEllipsoidFitter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EllipsoidFactory {

    /**
     * Creates a MarkEllipsoid using least-squares fitting to the points on the outline of an
     * object-mask
     *
     * @param object object-mask
     * @param dimensions the dimensions of the scene the object is contaiend in
     * @param suppressZCovariance whether to suppress the covariance in the z-dimension when doing
     *     least squares fiting
     * @param shellRad shellRad for the mark that is created
     * @return
     * @throws CreateException
     */
    public static Ellipsoid createMarkEllipsoidLeastSquares(
            ObjectMask object,
            Dimensions dimensions,
            boolean suppressZCovariance,
            double shellRad)
            throws CreateException {
        return createMarkEllipsoidLeastSquares(
                () -> PointsFromObject.listFromOutline3i(object),
                dimensions,
                suppressZCovariance,
                shellRad);
    }

    public static Ellipsoid createMarkEllipsoidLeastSquares(
            CheckedSupplier<List<Point3i>, CreateException> opPoints,
            Dimensions dimensions,
            boolean suppressZCovariance,
            double shellRad)
            throws CreateException {

        LinearLeastSquaresEllipsoidFitter pointsFitter = new LinearLeastSquaresEllipsoidFitter();
        pointsFitter.setShellRad(shellRad);
        pointsFitter.setSuppressZCovariance(suppressZCovariance);

        // Now get all the points on the outline
        Ellipsoid mark = new Ellipsoid();

        List<Point3f> pointsFloat =
                FunctionalList.mapToList(opPoints.get(), PointConverter::floatFromInt);

        try {
            pointsFitter.fit(pointsFloat, mark, dimensions);
        } catch (PointsFitterException e) {
            throw new CreateException(e);
        }
        return mark;
    }
}
