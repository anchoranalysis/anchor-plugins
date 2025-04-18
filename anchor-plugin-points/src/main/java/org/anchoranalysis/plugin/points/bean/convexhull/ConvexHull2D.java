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

package org.anchoranalysis.plugin.points.bean.convexhull;

import java.util.List;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.points.PointsFromMask;
import org.anchoranalysis.image.voxel.assigner.VoxelsAssigner;
import org.anchoranalysis.plugin.points.convexhull.ConvexHullUtilities;
import org.anchoranalysis.spatial.point.Point2i;

/**
 * Sets particular voxels to high only if they exist on the convex-hull of the outline of a mask.
 *
 * <p>All other voxels are low.
 *
 * <p>Uses the Gift wrap algorithm taken from FIJI's PolygonRoi.java
 *
 * @author Owen Feehan
 */
public class ConvexHull2D extends ConvexHullBase {

    @Override
    protected Mask createFromMask(Mask mask, Mask outline) throws ProvisionFailedException {
        try {
            List<Point2i> pointsOnConvexHull =
                    ConvexHullUtilities.convexHull2D(PointsFromMask.listFrom2i(outline));

            // Reuse the channel-created for the outline, to output the results
            changeMaskToShowPointsOnly(outline, pointsOnConvexHull);
            return outline;
        } catch (OperationFailedException | CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private void changeMaskToShowPointsOnly(Mask mask, List<Point2i> points) {

        VoxelsAssigner assignerOn = mask.assignOn();
        VoxelsAssigner assignerOff = mask.assignOff();

        assignerOff.toAll();

        points.forEach(point -> assignerOn.toVoxel(point.x(), point.y(), 0));
    }
}
