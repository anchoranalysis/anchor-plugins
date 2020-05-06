package org.anchoranalysis.plugin.image.obj.merge;

import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.plugin.image.obj.merge.priority.PrioritisedVertex;

public class GraphLogger {

	private DescribeGraph describeGraph;
	private LogErrorReporter logger;
	
	public GraphLogger(DescribeGraph describeGraph, LogErrorReporter logger) {
		super();
		this.describeGraph = describeGraph;
		this.logger = logger;
	}

	public void describeEdge(ObjVertex src, ObjVertex dest, ObjVertex merged, double priority, boolean doMerge) {
		log(
			describeGraph.describeEdge(src, dest, merged, priority, doMerge)
		);
	}

	public void describeMerge(ObjVertex omMerged, EdgeTypeWithVertices<ObjVertex, PrioritisedVertex> bestImprovement) {
		log(
			describeGraph.describeMerge(omMerged, bestImprovement)	
		);
	}
	
	public void logDescription() {
		log(
			describeGraph.describe()
		);
	}
	
	private void log(String str) {
		logger.getLogReporter().log( str );
	}

	public ErrorReporter getErrorReporter() {
		return logger.getErrorReporter();
	}


}
