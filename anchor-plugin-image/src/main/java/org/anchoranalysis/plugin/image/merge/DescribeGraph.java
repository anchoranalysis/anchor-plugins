package org.anchoranalysis.plugin.image.merge;

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

import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.plugin.image.merge.priority.PrioritisedVertex;

/**
 * Logs descriptions of the graph
 * 
 * @author FEEHANO
 *
 */
public class DescribeGraph {

	private NeighbourGraph graph;
	private boolean includePayload;
	
	
	/**
	 * Constructor
	 * 
	 * @param graph the graph to describe
	 * @param includePayload whether to include payload values in the log messages
	 */
	public DescribeGraph(NeighbourGraph graph, boolean includePayload) {
		super();
		this.graph = graph;
		this.includePayload = includePayload;
	}

	/**
	 * Generates a string describing the entire graph (vertices and edges)
	 * @return a multi-line string
	 */
	public String describe() {
		StringBuilder sb = new StringBuilder();
		describeAllVertices(sb);
		describeAllEdges(sb);
		return sb.toString();
	}
	
	
	/**
	 * Generates a string to describe a potential merge
	 * 
	 * @param src
	 * @param dest
	 * @param merged
	 * @param priority
	 * @param doMerge
	 * @return
	 */
	public String describeEdge( ObjVertex src, ObjVertex dest, ObjVertex merged, double priority, boolean doMerge ) {
		if (includePayload) {
			return String.format("Merge? %s (%f)\t+%s (%f)\t= %s (%f)\t%e  %s",
				src.getObjMask().centerOfGravity(),
				src.getPayload(),
				dest.getObjMask().centerOfGravity(),
				dest.getPayload(),
				merged.getObjMask().centerOfGravity(),
				merged.getPayload(),
				priority,
				doMerge ? "Yes!" : "No!"
			);
		} else {
			return String.format("Merge? %s\t+%s\t= %s\t%e  %s",
				src.getObjMask().centerOfGravity(),
				dest.getObjMask().centerOfGravity(),
				merged.getObjMask().centerOfGravity(),
				priority,
				doMerge ? "Yes!" : "No!"
			);
		}
	}
	
	public String describeMerge(
		ObjVertex omMerged,
		EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> bestImprovement
	) {
		if (includePayload) {
			return String.format("Merging %s and %s to %s (%f,%f->%f). Num edges/verices=%d,%d.",
				bestImprovement.getNode1().getObjMask().centerOfGravity(),
				bestImprovement.getNode2().getObjMask().centerOfGravity(),
				omMerged.getObjMask().centerOfGravity(),
				bestImprovement.getNode1().getPayload(),
				bestImprovement.getNode2().getPayload(),
				omMerged.getPayload(),
				graph.numVertices(),
				graph.numEdges()
			);
		} else {
			return String.format("Merging %s and %s to %s. Num edges/verices=%d,%d.",
				bestImprovement.getNode1().getObjMask().centerOfGravity(),
				bestImprovement.getNode2().getObjMask().centerOfGravity(),
				omMerged.getObjMask().centerOfGravity(),
				graph.numVertices(),
				graph.numEdges()
			);
		}
	}
	
	private void describeAllVertices(StringBuilder sb) {
		sb.append("START Vertices\n");
		for( ObjVertex vertex : graph.vertexSet() ) {
			sb.append(
				String.format("%s (%f)\n", vertex.getObjMask().centerOfGravity(), vertex.getPayload())
			);
		}
		sb.append("END Vertices\n");
	}
	
	private void describeAllEdges(StringBuilder sb) {
		sb.append("START Edges\n");
		for( EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> edge : graph.edgeSetUnique() ) {
			sb.append(
				describeEdge(
					edge.getNode1(),
					edge.getNode2(),
					edge.getEdge().getOmWithFeature(),
					edge.getEdge().getPriority(),
					edge.getEdge().isConsiderForMerge()
				)
			);
			sb.append("\n");
			
		}
		sb.append("END Edges");
	}
}
