/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.object.merge;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/**
 * Builds string-descriptions of a {@link NeighborGraph}.
 *
 * <p>This class provides methods to generate string descriptions of a {@link NeighborGraph}, its
 * vertices, edges, and potential merges.
 */
@AllArgsConstructor
public class DescribeGraph {

    /** Graph the {@link NeighborGraph} to describe. */
    private NeighborGraph graph;

    /** Whether to include payload values in the log messages. */
    private boolean includePayload;

    /**
     * Generates a string describing the entire graph (vertices and edges).
     *
     * @return a multi-line string description of the graph
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        describeAllVertices(sb);
        describeAllEdges(sb);
        return sb.toString();
    }

    /**
     * Generates a string to describe a potential merge.
     *
     * @param source the source {@link ObjectVertex}
     * @param destination the destination {@link ObjectVertex}
     * @param merged the resulting merged {@link ObjectVertex}
     * @param priority the priority of the merge
     * @param doMerge whether the merge will be performed
     * @return a string description of the potential merge
     */
    public String describeEdge(
            ObjectVertex source,
            ObjectVertex destination,
            ObjectVertex merged,
            double priority,
            boolean doMerge) {
        if (includePayload) {
            return String.format(
                    "Merge? %s (%f)\t+%s (%f)\t= %s (%f)\t%e  %s",
                    source.getObject(),
                    source.getPayload(),
                    destination.getObject(),
                    destination.getPayload(),
                    merged.getObject(),
                    merged.getPayload(),
                    priority,
                    doMerge ? "Yes!" : "No!");
        } else {
            return String.format(
                    "Merge? %s\t+%s\t= %s\t%e  %s",
                    source.getObject(),
                    destination.getObject(),
                    merged.getObject(),
                    priority,
                    doMerge ? "Yes!" : "No!");
        }
    }

    /**
     * Describes a merge operation that has been decided upon.
     *
     * @param merged the resulting merged {@link ObjectVertex}
     * @param bestImprovement the {@link TypedEdge} representing the best improvement for merging
     * @return a string description of the merge operation
     */
    public String describeMerge(
            ObjectVertex merged, TypedEdge<ObjectVertex, PrioritisedVertex> bestImprovement) {
        if (includePayload) {
            return String.format(
                    "Merging %s and %s to %s (%f,%f->%f). Num vertices=%d.",
                    bestImprovement.getFrom().getObject().centerOfGravity(),
                    bestImprovement.getTo().getObject().centerOfGravity(),
                    merged.getObject().centerOfGravity(),
                    bestImprovement.getFrom().getPayload(),
                    bestImprovement.getTo().getPayload(),
                    merged.getPayload(),
                    graph.numberVertices());
        } else {
            return String.format(
                    "Merging %s and %s to %s. Num vertices=%d.",
                    bestImprovement.getFrom().getObject().centerOfGravity(),
                    bestImprovement.getTo().getObject().centerOfGravity(),
                    merged.getObject().centerOfGravity(),
                    graph.numberVertices());
        }
    }

    private void describeAllVertices(StringBuilder builder) {
        builder.append("START Vertices\n");
        for (ObjectVertex vertex : graph.vertexSet()) {
            builder.append(
                    String.format(
                            "%s (%f)%n",
                            vertex.getObject().centerOfGravity(), vertex.getPayload()));
        }
        builder.append("END Vertices\n");
    }

    private void describeAllEdges(StringBuilder builder) {
        builder.append("START Edges\n");
        for (TypedEdge<ObjectVertex, PrioritisedVertex> edge : graph.edgeSetUnique()) {
            builder.append(
                    describeEdge(
                            edge.getFrom(),
                            edge.getTo(),
                            edge.getPayload().getVertex(),
                            edge.getPayload().getPriority(),
                            edge.getPayload().isConsiderForMerge()));
            builder.append("\n");
        }
        builder.append("END Edges");
    }
}
