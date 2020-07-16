/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import java.util.List;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.NullParamsBean;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;

public abstract class PointsFromOrientationProposer
        extends NullParamsBean<PointsFromOrientationProposer> {

    public abstract CreateProposalVisualization proposalVisualization(boolean detailed);

    public abstract void clearVisualizationState();

    // Optionally one can specify a maxDistance used to find a contour point, -1 disables
    public abstract List<List<Point3i>> calcPoints(
            Point3d centerPoint,
            Orientation orientation,
            boolean do3D,
            RandomNumberGenerator randomNumberGenerator,
            boolean forwardDirectionOnly)
            throws TraverseOutlineException;
}
