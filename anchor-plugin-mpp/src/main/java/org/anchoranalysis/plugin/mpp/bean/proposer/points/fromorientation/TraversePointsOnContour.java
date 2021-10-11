/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.plugin.mpp.bean.contour.TraverseOuterCounter;
import org.anchoranalysis.plugin.mpp.bean.contour.TraverseContourException;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.contour.FindPointOnContour;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;

public class TraversePointsOnContour extends PointsFromOrientationProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FindPointOnContour findOutlinePixelAngle;

    @BeanField @Getter @Setter private TraverseOuterCounter outlinePixelsRetriever;

    @BeanField @OptionalBean @Getter @Setter
    private TraverseOuterCounter outlinePixelsRetrieverReverse;
    // END BEAN PROPERTIES

    private List<Point3i> lastPointsForward = new ArrayList<>();
    private List<Point3i> lastPointsReverse = new ArrayList<>();

    // Calculates the points in both directions
    @Override
    public List<List<Point3i>> calculatePoints(
            Point3d centerPoint,
            Orientation orientation,
            boolean do3D,
            RandomNumberGenerator randomNumberGenerator,
            boolean forwardDirectionOnly)
            throws TraverseContourException {

        lastPointsForward.clear();
        lastPointsReverse.clear();

        addPointsFromOrientation(
                centerPoint,
                orientation,
                findOutlinePixelAngle,
                outlinePixelsRetriever,
                lastPointsForward,
                randomNumberGenerator);

        if (!forwardDirectionOnly) {
            TraverseOuterCounter reverseRetriever =
                    outlinePixelsRetrieverReverse != null
                            ? outlinePixelsRetrieverReverse
                            : outlinePixelsRetriever;

            addPointsFromOrientation(
                    centerPoint,
                    orientation.negative(),
                    findOutlinePixelAngle,
                    reverseRetriever,
                    lastPointsReverse,
                    randomNumberGenerator);

            if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
                throw new TraverseContourException(
                        "Cannot find forward or reverse point to traverse");
            }
        }

        if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
            throw new TraverseContourException("Cannot find outline point");
        }

        List<List<Point3i>> combinedLists = new ArrayList<>();
        combinedLists.add(lastPointsForward);
        if (!forwardDirectionOnly) {
            combinedLists.add(lastPointsReverse);
        }
        return combinedLists;
    }

    private Optional<Point3i> addPointsFromOrientation(
            Point3d centerPoint,
            Orientation orientation,
            FindPointOnContour find,
            TraverseOuterCounter traverseOutline,
            List<Point3i> listOut,
            RandomNumberGenerator randomNumberGenerator)
            throws TraverseContourException {

        try {
            Optional<Point3i> foundPoint = find.pointOnContour(centerPoint, orientation);

            if (foundPoint.isPresent()) {
                traverseOutline.traverse(foundPoint.get(), listOut, randomNumberGenerator);
            }

            return foundPoint;

        } catch (OperationFailedException e) {
            throw new TraverseContourException("Unable to add points from orientation", e);
        }
    }
}
