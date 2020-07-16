/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.priority;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;
import org.anchoranalysis.plugin.image.object.merge.GraphLogger;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

public abstract class AssignPriority {

    /**
     * Assigns a priority to a potential merge
     *
     * @param src source (first) object in the pair of objects that could be merged
     * @param dest destination (second) object in the pair of objects that could be merged
     * @param merge merged-object
     * @param payloadCalculator calculates the payload for any object
     * @param logger logger
     * @return the object with a priority afforded
     * @throws OperationFailedException
     */
    public PrioritisedVertex assignPriority(ObjectVertex src, ObjectVertex dest, GraphLogger logger)
            throws OperationFailedException {

        // Do merge
        ObjectMask merge = ObjectMaskMerger.merge(src.getObject(), dest.getObject());

        PrioritisedVertex withPriority =
                assignPriorityToEdge(src, dest, merge, logger.getErrorReporter());

        logger.describeEdge(
                src,
                dest,
                withPriority.getOmWithFeature(),
                withPriority.getPriority(),
                withPriority.isConsiderForMerge());

        return withPriority;
    }

    protected abstract PrioritisedVertex assignPriorityToEdge(
            ObjectVertex src, ObjectVertex dest, ObjectMask merge, ErrorReporter errorReporter)
            throws OperationFailedException;
}
