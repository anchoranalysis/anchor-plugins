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
