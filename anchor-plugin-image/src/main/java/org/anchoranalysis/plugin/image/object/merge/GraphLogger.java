/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge;

import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

public class GraphLogger {

    private DescribeGraph describeGraph;
    private Logger logger;

    public GraphLogger(DescribeGraph describeGraph, Logger logger) {
        super();
        this.describeGraph = describeGraph;
        this.logger = logger;
    }

    public void describeEdge(
            ObjectVertex src,
            ObjectVertex dest,
            ObjectVertex merged,
            double priority,
            boolean doMerge) {
        log(describeGraph.describeEdge(src, dest, merged, priority, doMerge));
    }

    public void describeMerge(
            ObjectVertex omMerged,
            EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> bestImprovement) {
        log(describeGraph.describeMerge(omMerged, bestImprovement));
    }

    public void logDescription() {
        log(describeGraph.describe());
    }

    private void log(String str) {
        logger.messageLogger().log(str);
    }

    public ErrorReporter getErrorReporter() {
        return logger.errorReporter();
    }
}
