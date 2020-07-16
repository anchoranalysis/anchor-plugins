/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import org.anchoranalysis.annotation.io.assignment.AssignmentMaskIntersection;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

public class MaskIntersectionAssigner
        extends AnnotationComparisonAssigner<AssignmentMaskIntersection> {

    @Override
    public AssignmentMaskIntersection createAssignment(
            ObjectsToCompare objectsToCompare,
            ImageDimensions dimensions,
            boolean useMIP,
            BoundIOContext context)
            throws CreateException {

        return new AssignmentMaskIntersection(
                extractSingleObj("left", objectsToCompare.getLeft()),
                extractSingleObj("right", objectsToCompare.getRight()));
    }

    private static ObjectMask extractSingleObj(String dscr, ObjectCollection objects)
            throws CreateException {
        if (objects.size() == 0) {
            throw new CreateException(
                    String.format("%s obj contains no objects. Exactly one must exist.", dscr));
        } else if (objects.size() > 1) {
            throw new CreateException(
                    String.format(
                            "%s obj contains %d objects. Only one is allowed.",
                            dscr, objects.size()));
        } else {
            return objects.get(0);
        }
    }

    @Override
    public AnnotationGroup<AssignmentMaskIntersection> groupForKey(String key) {
        return new AnnotationGroup<>(key);
    }

    @Override
    public boolean moreThanOneObj() {
        return false;
    }
}
