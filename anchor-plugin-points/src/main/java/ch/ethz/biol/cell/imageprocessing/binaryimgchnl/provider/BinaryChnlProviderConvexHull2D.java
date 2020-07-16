/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// USES Gift wrap algorithm taken from FIJI PolygonRoi.java

/**
 * Sets particular voxels to high only if they exist on the convex-hull of the outline of a mask.
 *
 * <p>All other voxels are low.
 *
 * @author Owen Feehan
 */
public class BinaryChnlProviderConvexHull2D extends ConvexHullBase {

    @Override
    protected Mask createFromChnl(Mask mask, Mask outline) throws CreateException {
        try {
            List<Point2i> pointsOnConvexHull =
                    ConvexHullUtilities.convexHull2D(
                            PointsFromBinaryChnl.pointsFromChnl2D(outline));

            // Reuse the channel-created for the outline, to output the results
            changeMaskToShowPointsOnly(outline, pointsOnConvexHull);
            return outline;
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private void changeMaskToShowPointsOnly(Mask mask, List<Point2i> points) {
        VoxelBox<?> voxels = mask.getChannel().getVoxelBox().any();

        int on = mask.getBinaryValues().getOnInt();
        int off = mask.getBinaryValues().getOffInt();

        voxels.setAllPixelsTo(off);

        points.forEach(point -> voxels.setVoxel(point.getX(), point.getY(), 0, on));
    }
}
