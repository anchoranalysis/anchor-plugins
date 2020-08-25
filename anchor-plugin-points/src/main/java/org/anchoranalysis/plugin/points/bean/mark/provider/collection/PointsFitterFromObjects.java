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

package org.anchoranalysis.plugin.points.bean.mark.provider.collection;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.mpp.bean.provider.MarkCollectionProvider;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.plugin.points.bean.fitter.PointsFitterToMark;
import org.anchoranalysis.plugin.points.convexhull.ConvexHullUtilities;

public class PointsFitterFromObjects extends MarkCollectionProvider {

    /// START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitterToMark pointsFitter;

    @BeanField @Getter @Setter private MarkFactory markFactory;

    /** If true, Reduces the set of points by applying a convex-hull operation */
    @BeanField @Getter @Setter private boolean convexHull = true;

    /**
     * If true, if too few points exist to make a mark, or otherwise a fitting errors, it is simply
     * not included (with only a log error) If false, an exception is thrown
     */
    @BeanField @Getter @Setter private boolean ignoreFittingFailure = true;
    // END BEAN PROPERTIES

    @Override
    public MarkCollection create() throws CreateException {

        Dimensions dimensions = pointsFitter.createDim();

        return new MarkCollection(
                pointsFitter.createObjects().stream()
                        .mapToListOptional(object -> createMarkFromObject(object, dimensions)));
    }

    private Optional<Mark> createMarkFromObject(ObjectMask object, Dimensions dimensions)
            throws CreateException {
        try {
            List<Point2i> points = maybeApplyConvexHull(object);
            if (points.isEmpty()) {
                return handleFittingFailure("There are 0 points to fit with.");
            }

            return fitToMark(PointConverter.convert2iTo3f(points), dimensions);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private List<Point2i> maybeApplyConvexHull(ObjectMask object) throws OperationFailedException {
        try {
            List<Point2i> points = ConvexHullUtilities.pointsOnOutline(object);
            if (convexHull) {
                return ConvexHullUtilities.convexHull2D(points, pointsFitter.getMinNumPoints());
            } else {
                return points;
            }
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private Optional<Mark> fitToMark(List<Point3f> pointsToFit, Dimensions dimensions)
            throws CreateException {

        Mark markOut = markFactory.create();

        try {
            pointsFitter.fitPointsToMark(pointsToFit, markOut, dimensions);
            return Optional.of(markOut);
        } catch (OperationFailedException e) {
            return handleFittingFailure(e.friendlyMessage());
        }
    }

    private Optional<Mark> handleFittingFailure(String errorMsg) throws CreateException {
        if (ignoreFittingFailure) {
            getLogger()
                    .messageLogger()
                    .logFormatted("Ignoring mark due to a fitting error. %s", errorMsg);
            return Optional.empty();
        } else {
            throw new CreateException(
                    String.format(
                            "Cannot create mark from points due to fitting error.%n%s", errorMsg));
        }
    }
}
