/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.priority;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/** Creates a priority between two vertices */
@FunctionalInterface
public interface Prioritizer {
    PrioritisedVertex createPriority(ObjectVertex src, ObjectVertex dest, Logger logger)
            throws OperationFailedException;
}
