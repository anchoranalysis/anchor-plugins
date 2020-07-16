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

package org.anchoranalysis.plugin.points.bean.fitter;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembership;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;

// Applies a points fitter on the convex hull of some objects
// Only works in 2D for now
public class MarkProviderPointsFitterFromConvexHull extends MarkProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitterToMark pointsFitter;

    @BeanField @Getter @Setter private MarkProvider markProvider;

    @BeanField @Getter @Setter private double minRatioPointsInsideRegion;

    @BeanField @Getter @Setter private RegionMap regionMap;

    @BeanField @Getter @Setter private int regionID = 0;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {

        List<Point3f> pointsForFitter = pointsForFitter();
        Optional<Mark> mark = markProvider.create();

        if (!mark.isPresent()) {
            return Optional.empty();
        }

        try {
            pointsFitter.fitPointsToMark(pointsForFitter, mark.get(), pointsFitter.createDim());
        } catch (OperationFailedException | CreateException e) {
            throw new CreateException(e);
        }

        if (minRatioPointsInsideRegion > 0) {
            // We perform a check that a minimum % of points are inside a particular region
            double ratioInside =
                    ratioPointsInsideRegion(mark.get(), pointsForFitter, regionMap, regionID);

            if (ratioInside < minRatioPointsInsideRegion) {
                return Optional.empty();
            }
        }
        return mark;
    }

    public static double ratioPointsInsideRegion(
            Mark m, List<Point3f> points, RegionMap regionMap, int regionID) {

        RegionMembership rm = regionMap.membershipForIndex(regionID);
        byte flags = rm.flags();

        int count = 0;
        for (Point3f point : points) {
            Point3d pointD = new Point3d(point);
            byte membership = m.evalPointInside(pointD);

            if (rm.isMemberFlag(membership, flags)) {
                count++;
            }
        }

        return ((double) count) / points.size();
    }

    private List<Point3f> pointsForFitter() throws CreateException {

        try {
            List<Point2i> selectedPoints =
                    ConvexHullUtilities.convexHull2D(
                            ConvexHullUtilities.pointsOnAllOutlines(pointsFitter.createObjects()),
                            pointsFitter.getMinNumPoints());
            return PointConverter.convert2iTo3f(selectedPoints);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
