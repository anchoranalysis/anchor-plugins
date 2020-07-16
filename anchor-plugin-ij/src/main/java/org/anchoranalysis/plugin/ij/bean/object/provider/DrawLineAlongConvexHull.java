/* (C)2020 */
package org.anchoranalysis.plugin.ij.bean.object.provider;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;
import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

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
public class DrawLineAlongConvexHull extends ObjectCollectionProviderWithDimensions {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        ImageDimensions dimensions = createDimensions();
        return objects.stream().map(object -> transform(object, dimensions));
    }

    private ObjectMask transform(ObjectMask object, ImageDimensions dimensions)
            throws CreateException {
        try {
            List<Point2i> pointsConvexHull =
                    ConvexHullUtilities.convexHull2D(ConvexHullUtilities.pointsOnOutline(object));

            if (pointsConvexHull.size() <= 1) {
                return object;
            }

            return WalkShortestPath.walkLine(PointConverter.convert2iTo3d(pointsConvexHull));
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
