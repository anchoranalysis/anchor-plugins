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

package org.anchoranalysis.plugin.points.bean.mask.provider;

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
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.assigner.VoxelsAssigner;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsByte;

public class ConvexHull3D extends ConvexHullBase {

    @Override
    protected Mask createFromMask(Mask maskIn, Mask outline) throws CreateException {
        MessageLogger logger = getLogger().messageLogger();
        List<Point3d> extPoints = pointsFromMask(outline);

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
        Channel out = outline.channel();
        Voxels<ByteBuffer> voxelsOut = out.voxels().asByte();

        VoxelsAssigner assignerOn = voxelsOut.assignValue(outline.binaryValues().getOnInt());
        VoxelsAssigner assignerOff = voxelsOut.assignValue(outline.binaryValues().getOffInt());

        assignerOff.toAll();
        for (int i = 0; i < vertices.length; i++) {

            // Note this is not the usual {@code Point3d} type we use
            Point3d point = vertices[i];

            assignerOn.toVoxel((int) point.x, (int) point.y, (int) point.z);
        }

        return outline;
    }

    // We use it here as it uses the quickHull3D Point3d primitive
    private static List<Point3d> pointsFromMask(Mask mask) {
        List<Point3d> listOut = new ArrayList<>();
        BinaryValuesByte bvb = mask.binaryValues().createByte();
        IterateVoxelsByte.iterateEqualValues(mask.voxels(), bvb.getOnByte(), Point3d::new);
        return listOut;
    }
}
