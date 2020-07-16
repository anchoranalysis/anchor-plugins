/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge;

import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/**
 * Logs descriptions of the graph
 *
 * @author Owen Feehan
 */
public class DescribeGraph {

    private NeighborGraph graph;
    private boolean includePayload;

    /**
     * Constructor
     *
     * @param graph the graph to describe
     * @param includePayload whether to include payload values in the log messages
     */
    public DescribeGraph(NeighborGraph graph, boolean includePayload) {
        super();
        this.graph = graph;
        this.includePayload = includePayload;
    }

    /**
     * Generates a string describing the entire graph (vertices and edges)
     *
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
    public String describeEdge(
            ObjectVertex src,
            ObjectVertex dest,
            ObjectVertex merged,
            double priority,
            boolean doMerge) {
        if (includePayload) {
            return String.format(
                    "Merge? %s (%f)\t+%s (%f)\t= %s (%f)\t%e  %s",
                    src.getObject(),
                    src.getPayload(),
                    dest.getObject(),
                    dest.getPayload(),
                    merged.getObject(),
                    merged.getPayload(),
                    priority,
                    doMerge ? "Yes!" : "No!");
        } else {
            return String.format(
                    "Merge? %s\t+%s\t= %s\t%e  %s",
                    src.getObject(),
                    dest.getObject(),
                    merged.getObject(),
                    priority,
                    doMerge ? "Yes!" : "No!");
        }
    }

    public String describeMerge(
            ObjectVertex omMerged,
            EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> bestImprovement) {
        if (includePayload) {
            return String.format(
                    "Merging %s and %s to %s (%f,%f->%f). Num vertices/edges=%d,%d.",
                    bestImprovement.getNode1().getObject().centerOfGravity(),
                    bestImprovement.getNode2().getObject().centerOfGravity(),
                    omMerged.getObject().centerOfGravity(),
                    bestImprovement.getNode1().getPayload(),
                    bestImprovement.getNode2().getPayload(),
                    omMerged.getPayload(),
                    graph.numVertices(),
                    graph.numEdges());
        } else {
            return String.format(
                    "Merging %s and %s to %s. Num vertices/edges=%d,%d.",
                    bestImprovement.getNode1().getObject().centerOfGravity(),
                    bestImprovement.getNode2().getObject().centerOfGravity(),
                    omMerged.getObject().centerOfGravity(),
                    graph.numVertices(),
                    graph.numEdges());
        }
    }

    private void describeAllVertices(StringBuilder sb) {
        sb.append("START Vertices\n");
        for (ObjectVertex vertex : graph.vertexSet()) {
            sb.append(
                    String.format(
                            "%s (%f)%n",
                            vertex.getObject().centerOfGravity(), vertex.getPayload()));
        }
        sb.append("END Vertices\n");
    }

    private void describeAllEdges(StringBuilder sb) {
        sb.append("START Edges\n");
        for (EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> edge : graph.edgeSetUnique()) {
            sb.append(
                    describeEdge(
                            edge.getNode1(),
                            edge.getNode2(),
                            edge.getEdge().getOmWithFeature(),
                            edge.getEdge().getPriority(),
                            edge.getEdge().isConsiderForMerge()));
            sb.append("\n");
        }
        sb.append("END Edges");
    }
}
