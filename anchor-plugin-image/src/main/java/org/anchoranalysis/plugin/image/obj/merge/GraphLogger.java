package org.anchoranalysis.plugin.image.obj.merge;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
