/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;

public class MergeLists extends PointsFromOrientationProposer {

    // START BEAN PROPERTIES
    @BeanField private PointsFromOrientationProposer pointsFromOrientationProposer;
    // END BEAN PROPERTIEs

    @Override
    public CreateProposalVisualization proposalVisualization(boolean detailed) {
        return pointsFromOrientationProposer.proposalVisualization(detailed);
    }

    @Override
    public void clearVisualizationState() {
        pointsFromOrientationProposer.clearVisualizationState();
    }

    @Override
    public List<List<Point3i>> calcPoints(
            Point3d centerPoint,
            Orientation orientation,
            boolean do3D,
            RandomNumberGenerator randomNumberGenerator,
            boolean forwardDirectionOnly)
            throws TraverseOutlineException {

        List<List<Point3i>> listOfLists =
                pointsFromOrientationProposer.calcPoints(
                        centerPoint,
                        orientation,
                        do3D,
                        randomNumberGenerator,
                        forwardDirectionOnly);

        List<Point3i> combinedList = new ArrayList<>();
        for (List<Point3i> list : listOfLists) {
            combinedList.addAll(list);
        }

        List<List<Point3i>> listOfListsNew = new ArrayList<>();
        listOfListsNew.add(combinedList);
        return listOfListsNew;
    }

    public PointsFromOrientationProposer getPointsFromOrientationProposer() {
        return pointsFromOrientationProposer;
    }

    public void setPointsFromOrientationProposer(
            PointsFromOrientationProposer pointsFromOrientationProposer) {
        this.pointsFromOrientationProposer = pointsFromOrientationProposer;
    }
}
