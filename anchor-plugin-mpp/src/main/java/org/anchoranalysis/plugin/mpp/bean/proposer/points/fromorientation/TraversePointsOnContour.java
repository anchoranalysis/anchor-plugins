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

import static org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.VisualizationUtilities.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.plugin.mpp.bean.outline.OutlinePixelsRetriever;
import org.anchoranalysis.plugin.mpp.bean.outline.TraverseOutlineException;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.onoutline.FindPointOnOutline;

public class TraversePointsOnContour extends PointsFromOrientationProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FindPointOnOutline findOutlinePixelAngle;

    @BeanField @Getter @Setter private OutlinePixelsRetriever outlinePixelsRetriever;

    @BeanField @OptionalBean @Getter @Setter
    private OutlinePixelsRetriever outlinePixelsRetrieverReverse;
    // END BEAN PROPERTIES

    private boolean do3D;
    private List<Point3i> lastPointsForward = new ArrayList<>();
    private List<Point3i> lastPointsReverse = new ArrayList<>();
    private Optional<Point3i> forwardCenterPoint;
    private Optional<Point3i> reverseCenterPoint;

    // Calculates the points in both directions
    @Override
    public List<List<Point3i>> calculatePoints(
            Point3d centerPoint,
            Orientation orientation,
            boolean do3D,
            RandomNumberGenerator randomNumberGenerator,
            boolean forwardDirectionOnly)
            throws TraverseOutlineException {

        this.do3D = do3D;

        lastPointsForward.clear();
        lastPointsReverse.clear();

        forwardCenterPoint =
                addPointsFromOrientation(
                        centerPoint,
                        orientation,
                        findOutlinePixelAngle,
                        outlinePixelsRetriever,
                        lastPointsForward,
                        randomNumberGenerator);

        if (!forwardDirectionOnly) {
            OutlinePixelsRetriever reverseRetriever =
                    outlinePixelsRetrieverReverse != null
                            ? outlinePixelsRetrieverReverse
                            : outlinePixelsRetriever;
            reverseCenterPoint =
                    addPointsFromOrientation(
                            centerPoint,
                            orientation.negative(),
                            findOutlinePixelAngle,
                            reverseRetriever,
                            lastPointsReverse,
                            randomNumberGenerator);

            if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
                throw new TraverseOutlineException(
                        "Cannot find forward or reverse point to traverse");
            }
        }

        if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
            throw new TraverseOutlineException("Cannot find outline point");
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
            FindPointOnOutline find,
            OutlinePixelsRetriever traverseOutline,
            List<Point3i> listOut,
            RandomNumberGenerator randomNumberGenerator)
            throws TraverseOutlineException {

        try {
            Optional<Point3i> foundPoint = find.pointOnOutline(centerPoint, orientation);

            if (foundPoint.isPresent()) {
                traverseOutline.traverse(foundPoint.get(), listOut, randomNumberGenerator);
            }

            return foundPoint;

        } catch (OperationFailedException e) {
            throw new TraverseOutlineException("Unable to add points from orientation", e);
        }
    }

    public CreateProposalVisualization proposalVisualization(boolean detailed) {
        return marks -> {
            maybeAddPoints(marks, lastPointsForward, Color.CYAN);
            maybeAddPoints(marks, lastPointsReverse, Color.YELLOW);

            if (detailed) {
                maybeAddConic(marks, forwardCenterPoint, Color.MAGENTA, do3D);
                maybeAddConic(marks, reverseCenterPoint, Color.MAGENTA, do3D);
                maybeAddLineSegment(marks, forwardCenterPoint, reverseCenterPoint, Color.ORANGE);
            }
        };
    }

    @Override
    public void clearVisualizationState() {
        lastPointsForward.clear();
        lastPointsReverse.clear();
        forwardCenterPoint = Optional.empty();
        reverseCenterPoint = Optional.empty();
    }
}
