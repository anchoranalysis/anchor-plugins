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

package org.anchoranalysis.plugin.points.bean;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.mpp.bean.provider.MarkCollectionProvider;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.plugin.points.bean.fitter.PointsFitterToMark;
import org.anchoranalysis.plugin.points.convexhull.ConvexHullUtilities;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.PointConverter;

/** A {@link MarkCollectionProvider} that fits points from objects to create marks. */
public class FitPointsFromObjects extends MarkCollectionProvider {

    // START BEAN PROPERTIES
    /** The {@link PointsFitterToMark} used to fit points to marks. */
    @BeanField @Getter @Setter private PointsFitterToMark pointsFitter;

    /** The {@link MarkFactory} used to create new marks. */
    @BeanField @Getter @Setter private MarkFactory markFactory;

    /** If true, reduces the set of points by applying a convex-hull operation. */
    @BeanField @Getter @Setter private boolean convexHull = true;

    /**
     * If true, ignores fitting failures and continues processing. If false, throws an exception on
     * fitting failure.
     */
    @BeanField @Getter @Setter private boolean ignoreFittingFailure = true;

    // END BEAN PROPERTIES

    @Override
    public MarkCollection get() throws ProvisionFailedException {

        try {
            Dimensions dimensions = pointsFitter.createDim();

            return new MarkCollection(
                    pointsFitter.createObjects().stream()
                            .mapToListOptional(object -> createMarkFromObject(object, dimensions)));
        } catch (ProvisionFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates a mark from an object mask.
     *
     * @param object the {@link ObjectMask} to create a mark from
     * @param dimensions the {@link Dimensions} of the image space
     * @return an {@link Optional} containing the created {@link Mark}, or empty if creation failed
     * @throws ProvisionFailedException if mark creation fails and ignoreFittingFailure is false
     */
    private Optional<Mark> createMarkFromObject(ObjectMask object, Dimensions dimensions)
            throws ProvisionFailedException {
        try {
            List<Point2i> points = maybeApplyConvexHull(object);
            if (points.isEmpty()) {
                return handleFittingFailure("There are 0 points to fit with.");
            }

            return fitToMark(PointConverter.convert2iTo3f(points), dimensions);

        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Applies the convex hull operation to the object's outline points if convexHull is true.
     *
     * @param object the {@link ObjectMask} to extract points from
     * @return a {@link List} of {@link Point2i} representing the object's outline or convex hull
     * @throws OperationFailedException if the convex hull operation fails
     */
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

    /**
     * Fits points to a mark using the pointsFitter.
     *
     * @param pointsToFit the {@link List} of {@link Point3f} to fit
     * @param dimensions the {@link Dimensions} of the image space
     * @return an {@link Optional} containing the fitted {@link Mark}, or empty if fitting failed
     * @throws ProvisionFailedException if mark fitting fails and ignoreFittingFailure is false
     */
    private Optional<Mark> fitToMark(List<Point3f> pointsToFit, Dimensions dimensions)
            throws ProvisionFailedException {

        Mark markOut = markFactory.create();

        try {
            pointsFitter.fitPointsToMark(pointsToFit, markOut, dimensions);
            return Optional.of(markOut);
        } catch (OperationFailedException e) {
            return handleFittingFailure(e.friendlyMessage());
        }
    }

    /**
     * Handles fitting failures based on the ignoreFittingFailure flag.
     *
     * @param errorMessage the error message describing the fitting failure
     * @return an {@link Optional} that is empty if ignoreFittingFailure is true
     * @throws ProvisionFailedException if ignoreFittingFailure is false
     */
    private Optional<Mark> handleFittingFailure(String errorMessage)
            throws ProvisionFailedException {
        if (ignoreFittingFailure) {
            getLogger()
                    .messageLogger()
                    .logFormatted("Ignoring mark due to a fitting error. %s", errorMessage);
            return Optional.empty();
        } else {
            throw new ProvisionFailedException(
                    String.format(
                            "Cannot create mark from points due to fitting error.%n%s",
                            errorMessage));
        }
    }
}
