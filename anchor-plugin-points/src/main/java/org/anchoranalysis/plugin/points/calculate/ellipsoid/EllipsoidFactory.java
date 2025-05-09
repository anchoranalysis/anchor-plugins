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
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.points.PointsFromObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.plugin.points.bean.fitter.LinearLeastSquaresEllipsoidFitter;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;

/** Factory for creating {@link Ellipsoid} objects using least-squares fitting. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EllipsoidFactory {

    /**
     * Creates a {@link Ellipsoid} using least-squares fitting to the points on the outline of an
     * {@link ObjectMask}.
     *
     * @param object object-mask
     * @param dimensions the dimensions of the scene the object is contained in
     * @param suppressZCovariance whether to suppress the covariance in the z-dimension when doing
     *     least squares fitting
     * @param shell shell for the mark that is created
     * @return the created {@link Ellipsoid}
     * @throws CreateException if the ellipsoid creation fails
     */
    public static Ellipsoid createMarkEllipsoidLeastSquares(
            ObjectMask object, Dimensions dimensions, boolean suppressZCovariance, double shell)
            throws CreateException {
        return createMarkEllipsoidLeastSquares(
                () -> PointsFromObject.listFromOutline3i(object),
                dimensions,
                suppressZCovariance,
                shell);
    }

    /**
     * Creates a {@link Ellipsoid} using least-squares fitting to a supplied list of points.
     *
     * @param opPoints supplier for the list of points to fit
     * @param dimensions the dimensions of the scene the object is contained in
     * @param suppressZCovariance whether to suppress the covariance in the z-dimension when doing
     *     least squares fitting
     * @param shell shell for the mark that is created
     * @return the created {@link Ellipsoid}
     * @throws CreateException if the ellipsoid creation fails
     */
    public static Ellipsoid createMarkEllipsoidLeastSquares(
            CheckedSupplier<List<Point3i>, CreateException> opPoints,
            Dimensions dimensions,
            boolean suppressZCovariance,
            double shell)
            throws CreateException {

        LinearLeastSquaresEllipsoidFitter pointsFitter = new LinearLeastSquaresEllipsoidFitter();
        pointsFitter.setShell(shell);
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
