/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

import static org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.VisualizationUtilities.*;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.OutlinePixelsRetriever;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;
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
    public List<List<Point3i>> calcPoints(
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
        return cfg -> {
            maybeAddPoints(cfg, lastPointsForward, Color.CYAN);
            maybeAddPoints(cfg, lastPointsReverse, Color.YELLOW);

            if (detailed) {
                maybeAddConic(cfg, forwardCenterPoint, Color.MAGENTA, do3D);
                maybeAddConic(cfg, reverseCenterPoint, Color.MAGENTA, do3D);
                maybeAddLineSegment(cfg, forwardCenterPoint, reverseCenterPoint, Color.ORANGE);
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
