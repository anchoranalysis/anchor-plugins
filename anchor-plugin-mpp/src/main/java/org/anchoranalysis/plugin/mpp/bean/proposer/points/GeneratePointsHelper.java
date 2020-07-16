/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;

@AllArgsConstructor
class GeneratePointsHelper {

    private Point3d pointRoot;
    private final Optional<Mask> chnlFilled;
    private int maxZDistance;
    private int skipZDistance;
    private Mask chnl;
    private ImageDimensions dimensions;

    public List<Point3i> generatePoints(List<List<Point3i>> pointsXY)
            throws OperationFailedException {
        // We take the first point in each list, as where it intersects with the edge
        PointListForConvex pointList = pointListForConvex(pointsXY);

        List<Point3i> lastPointsAll = new ArrayList<>();

        for (List<Point3i> contourPoints : pointsXY) {
            lastPointsAll.addAll(extendedPoints(contourPoints, pointList));
        }
        return lastPointsAll;
    }

    private List<Point3i> extendedPoints(
            List<Point3i> pointsAlongContour, PointListForConvex pointList)
            throws OperationFailedException {

        BoundingBox bbox = BoundingBoxFromPoints.forList(pointsAlongContour);

        int zLow = Math.max(0, bbox.cornerMin().getZ() - maxZDistance);
        int zHigh = Math.min(dimensions.getZ(), bbox.cornerMin().getZ() + maxZDistance);

        if (chnlFilled.isPresent()) {
            return new PointsFromInsideHelper(pointList, chnlFilled.get(), bbox)
                    .convexOnly(chnl, pointRoot, skipZDistance);
        } else {
            return PointsFromBinaryChnl.pointsFromChnlInsideBox(
                    chnl,
                    bbox.duplicateChangeZ(zLow, zHigh - zLow),
                    (int) Math.floor(pointRoot.getZ()),
                    skipZDistance);
        }
    }

    private static PointListForConvex pointListForConvex(List<List<Point3i>> points) {
        PointListForConvex pl = new PointListForConvex();
        for (List<Point3i> list : points) {
            if (!list.isEmpty()) {
                pl.add(list.get(0));
            }
        }
        return pl;
    }
}
