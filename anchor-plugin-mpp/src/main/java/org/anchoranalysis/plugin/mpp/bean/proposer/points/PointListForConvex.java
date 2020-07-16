/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler.VisitSchedulerConvexAboutRoot;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;

class PointListForConvex {

    private List<Point3i> list = new ArrayList<>();

    public boolean add(Point3i e) {
        return list.add(e);
    }

    public boolean convexWithAtLeastOnePoint(
            Point3i root, Point3i point, BinaryVoxelBox<ByteBuffer> vb) {
        return VisitSchedulerConvexAboutRoot.isPointConvexTo(root, point, vb);
    }

    public boolean convexWithAtLeastOnePoint(Point3i pointToHave, BinaryVoxelBox<ByteBuffer> vb) {
        return list.stream().anyMatch(point -> convexWithAtLeastOnePoint(point, pointToHave, vb));
    }
}
