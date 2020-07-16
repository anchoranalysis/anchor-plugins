/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

public abstract class AnnotationComparisonAssigner<T extends Assignment>
        extends AnchorBean<AnnotationComparisonAssigner<T>> {

    public abstract T createAssignment(
            ObjectsToCompare objectsToCompare,
            ImageDimensions dimensions,
            boolean useMIP,
            BoundIOContext context)
            throws CreateException;

    public abstract AnnotationGroup<T> groupForKey(String key);

    /** Can more than one object exist? */
    public abstract boolean moreThanOneObj();
}
