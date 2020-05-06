package org.anchoranalysis.plugin.image.obj.merge.priority;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.plugin.image.obj.merge.ObjVertex;

/** Creates a priority between two vertices */
@FunctionalInterface
public interface Prioritizer {
	PrioritisedVertex createPriority(ObjVertex src, ObjVertex dest, LogErrorReporter logger) throws OperationFailedException;
}