/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class BinaryChnlProviderConvexHull3D extends ConvexHullBase {

    @Override
    protected Mask createFromChnl(Mask chnlIn, Mask outline) throws CreateException {
        MessageLogger logger = getLogger().messageLogger();
        List<Point3d> extPoints = pointsFromChnl(outline);

        Point3d[] pointArr = extPoints.toArray(new Point3d[] {});

        QuickHull3D hull = new QuickHull3D();
        hull.build(pointArr);

        logger.log("Vertices:");
        Point3d[] vertices = hull.getVertices();
        for (int i = 0; i < vertices.length; i++) {
            Point3d point = vertices[i];
            logger.log(point.x + " " + point.y + " " + point.z);
        }

        logger.log("Faces:");
        int[][] faceIndices = hull.getFaces();
        for (int i = 0; i < faceIndices.length; i++) {
            for (int k = 0; k < faceIndices[i].length; k++) {
                logger.log(faceIndices[i][k] + " ");
            }
            logger.log("");
        }

        // we write the vertices to the outline
        Channel out = outline.getChannel();
        VoxelBox<ByteBuffer> vbOut = out.getVoxelBox().asByte();

        vbOut.setAllPixelsTo(outline.getBinaryValues().getOffInt());
        for (int i = 0; i < vertices.length; i++) {
            Point3d point = vertices[i];
            vbOut.setVoxel(
                    (int) point.x,
                    (int) point.y,
                    (int) point.z,
                    outline.getBinaryValues().getOnInt());
        }

        return outline;
    }

    // We use it here as it uses the quickHull3D Point3d primitive
    private static List<Point3d> pointsFromChnl(Mask chnl) {

        List<Point3d> listOut = new ArrayList<>();

        BinaryValuesByte bvb = chnl.getBinaryValues().createByte();

        Extent e = chnl.getVoxelBox().extent();
        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bb = chnl.getVoxelBox().getPixelsForPlane(z).buffer();

            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    if (bb.get() == bvb.getOnByte()) {
                        listOut.add(new Point3d(x, y, z));
                    }
                }
            }
        }

        return listOut;
    }
}
