package org.anchoranalysis.bean.provider.objs.merge;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.graph.GraphWithEdgeTypes.EdgeTypeWithVertices;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;

/**
 * Logs descriptions of the graph
 * 
 * @author FEEHANO
 *
 */
public class DescribeGraph {

	private NeighbourGraph graph;
	
	/**
	 * Include single-feature values in the log file?
	 */
	private boolean logSingleFeature;
	
	public DescribeGraph(NeighbourGraph graph, boolean logSingleFeature) {
		super();
		this.graph = graph;
		this.logSingleFeature = logSingleFeature;
	}

	public String describeEdge( ObjMaskWithFeature node1, ObjMaskWithFeature node2, ObjMaskWithFeature omMerged, double priority, boolean doMerge ) {
		if (logSingleFeature) {
			return String.format("Merge? %s (%f)\t+%s (%f)\t= %s (%f)\t%e  %s",
				node1.getObjMask().centerOfGravity(),
				node1.getFeatureVal(),
				node2.getObjMask().centerOfGravity(),
				node2.getFeatureVal(),
				omMerged.getObjMask().centerOfGravity(),
				omMerged.getFeatureVal(),
				priority,
				doMerge ? "Yes!" : "No!"
			);
		} else {
			return String.format("Merge? %s\t+%s\t= %s\t%e  %s",
				node1.getObjMask().centerOfGravity(),
				node2.getObjMask().centerOfGravity(),
				omMerged.getObjMask().centerOfGravity(),
				priority,
				doMerge ? "Yes!" : "No!"
			);
		}
	}
	
	public String describeMerge(
		ObjMaskWithFeature omMerged,
		EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> bestImprovement
	) {
		if (logSingleFeature) {
			return String.format("Merging %s and %s to %s (%f,%f->%f). Num edges/verices=%d,%d.",
				bestImprovement.getNode1().getObjMask().centerOfGravity(),
				bestImprovement.getNode2().getObjMask().centerOfGravity(),
				omMerged.getObjMask().centerOfGravity(),
				bestImprovement.getNode1().getFeatureVal(),
				bestImprovement.getNode2().getFeatureVal(),
				omMerged.getFeatureVal(),
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
	
	
	public String description() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("START Vertices\n");
		for( ObjMaskWithFeature vertex : graph.vertexSet() ) {
			sb.append(
				String.format("%s (%f)\n", vertex.getObjMask().centerOfGravity(), vertex.getFeatureVal())
			);
		}
		sb.append("END Vertices\n");
		
		
		sb.append("START Edges\n");
		for( EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> edge : graph.edgeSetUnique() ) {
			sb.append( describeEdge(edge.getNode1(),edge.getNode2(),edge.getEdge().getOmWithFeature(), edge.getEdge().getPriority(), edge.getEdge().isConsiderForMerge() ) );
			sb.append("\n");
			
		}
		sb.append("END Edges");
		
		return sb.toString();
	}
	
	
}
