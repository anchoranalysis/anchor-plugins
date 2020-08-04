/*-
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.image.points.PointsFromMask;

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
            return PointsFromMask.listFromSlicesInsideBox3i(
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
