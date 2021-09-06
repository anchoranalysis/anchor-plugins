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

package org.anchoranalysis.plugin.imagej.bean.object.provider;

import java.util.List;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.points.convexhull.ConvexHullUtilities;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * Draws a lines between successive points on the convex-hull of an object.
 *
 * <p>For each object: 1. extracts the convex hull of the outline (a set of points) 2. connects
 * these points together by walking a line between them. This ensures it is a single connected
 * component.
 *
 * <p>This only currently works in 2D on coplanar (XY) points.
 *
 * @author feehano
 */
public class DrawLineAlongConvexHull extends ObjectCollectionProviderUnary {

    @Override
    protected ObjectCollection createFromObjects(ObjectCollection objects) throws ProvisionFailedException {
        return objects.stream().map(this::transform);
    }

    private ObjectMask transform(ObjectMask object) throws ProvisionFailedException {
        try {
            List<Point2i> pointsConvexHull =
                    ConvexHullUtilities.convexHull2D(ConvexHullUtilities.pointsOnOutline(object));

            if (pointsConvexHull.size() <= 1) {
                return object;
            }

            return WalkShortestPath.walkLine(PointConverter.convert2iTo3d(pointsConvexHull));
        } catch (OperationFailedException | CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
