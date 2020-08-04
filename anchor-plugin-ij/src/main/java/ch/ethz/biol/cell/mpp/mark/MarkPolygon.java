/*-
 * #%L
 * anchor-plugin-ij
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

package ch.ethz.biol.cell.mpp.mark;

import com.google.common.base.Preconditions;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.util.List;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipUtilities;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPointList;
import org.anchoranalysis.core.error.OptionalOperationUnsupportedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;

public class MarkPolygon extends MarkAbstractPointList {

    /** */
    private static final long serialVersionUID = 1718294470056379145L;

    private static final byte FLAG_OUTSIDE = RegionMembershipUtilities.flagForNoRegion();
    private static final byte FLAG_SUBMARK_INSIDE =
            RegionMembershipUtilities.flagForRegion(GlobalRegionIdentifiers.SUBMARK_INSIDE);

    private PolygonRoi polygon;

    private double area;
    private Point3d centroid;

    @Override
    public byte evalPointInside(Point3d pt) {

        // FOR NOW WE IGNORE THE SHELL RADIUS
        if (containsPixel(pt.x(), pt.y()) || getPoints().contains(pt)) {
            return FLAG_SUBMARK_INSIDE;
        }

        return FLAG_OUTSIDE;
    }

    private boolean containsPixel(double xDbl, double yDbl) {
        int x = (int) xDbl;
        int y = (int) yDbl;
        return contains(x, y) || contains(x, y + 1) || contains(x + 1, y) || contains(x + 1, y + 1);
    }

    private boolean contains(int x, int y) {
        return polygon.contains(x, y);
    }

    @Override
    public Mark duplicate() {
        MarkPolygon out = new MarkPolygon();
        doDuplicate(out);
        return out;
    }

    @Override
    public double volume(int regionID) {
        return area;
    }

    @Override
    public String toString() {
        return "polygon";
    }

    @Override
    public void scale(double multFactor) throws OptionalOperationUnsupportedException {
        throw new OptionalOperationUnsupportedException("Not supported");
    }

    @Override
    public int numDims() {
        return 2;
    }

    @Override
    public Point3d centerPoint() {
        return centroid;
    }

    @Override
    public String getName() {
        return "polygon";
    }

    @Override
    public void updateAfterPointsChange() {
        super.updateAfterPointsChange();
        Preconditions.checkArgument(!getPoints().isEmpty());

        this.area = calcArea(getPoints());
        this.centroid = calcCentroid(getPoints(), area);

        polygon = createRoi(getPoints());
    }

    private static PolygonRoi createRoi(List<Point3d> points) {

        float[] xArr = new float[points.size()];
        float[] yArr = new float[points.size()];

        int i = 0;
        for (Point3d point : points) {
            xArr[i] = (float) point.x();
            yArr[i] = (float) point.y();
            i++;
        }

        return new PolygonRoi(xArr, yArr, points.size(), Roi.POLYGON);
    }

    private static double calcArea(List<Point3d> points) {
        // http://en.wikipedia.org/wiki/Polygon

        int numPoints = points.size();

        double sum = 0;

        for (int i = 0; i < numPoints; i++) {
            Point3d point = points.get(i);

            // We cycle around at the end
            Point3d pointNext = i == (numPoints - 1) ? points.get(0) : points.get(i + 1);

            sum += (point.x() * pointNext.y()) - (pointNext.x() * point.y());
        }
        return sum / 2;
    }

    private static Point3d calcCentroid(List<Point3d> points, double area) {
        // http://en.wikipedia.org/wiki/Polygon
        // Shoelace formula

        int numPoints = points.size();

        Point3d centroid = new Point3d();

        for (int i = 0; i < numPoints; i++) {
            Point3d point = points.get(i);

            // We cycle around at the end
            Point3d pointNext = i == (numPoints - 1) ? points.get(0) : points.get(i + 1);

            double leftX = point.x() + pointNext.x();
            double leftY = point.y() + pointNext.y();
            double right = (point.x() * pointNext.y()) - (pointNext.x() * point.y());

            centroid.setX(centroid.x() + leftX * right);
            centroid.setY(centroid.y() + leftY * right);
        }

        centroid.scale(1 / (6 * area));

        return centroid;
    }

    @Override
    public int numRegions() {
        return 1;
    }

    @Override
    public BoundingBox boxAllRegions(ImageDimensions bndScene) {
        return box(bndScene, GlobalRegionIdentifiers.SUBMARK_INSIDE);
    }
}
