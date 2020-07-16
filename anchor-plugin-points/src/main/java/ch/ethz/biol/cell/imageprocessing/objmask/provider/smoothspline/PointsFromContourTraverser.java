/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.traverser.OutlineTraverser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PointsFromContourTraverser {

    /** Extracts a list of points from the outline (contour) of the object-mask */
    public static List<Point3i> pointsFromContour(ObjectMask object)
            throws OperationFailedException {

        List<Point3i> pointsTraversed = new ArrayList<>();

        OutlineTraverser outline =
                new OutlineTraverser(object.duplicate(), (a, b) -> true, false, true);
        outline.applyGlobal(pointsTraversed);

        return pointsTraversed;
    }
}
