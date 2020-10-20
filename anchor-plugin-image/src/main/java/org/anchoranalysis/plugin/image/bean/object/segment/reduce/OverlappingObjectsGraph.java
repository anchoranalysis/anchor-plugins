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
package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.Collection;
import java.util.List;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.graph.GraphWithoutPayload;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * A graph of overlapping-objects.
 *
 * @author Owen Feehan
 */
class OverlappingObjectsGraph {

    private final GraphWithoutPayload<ObjectMask> graph;

    /**
     * Creates graph of overlapping objects from a list of elements.
     *
     * <p>Each object becomes a vertex in the graph, and when an object overlaps with another an
     * edge connects the two vertices.
     *
     * @param elements the elements
     */
    public OverlappingObjectsGraph(List<WithConfidence<ObjectMask>> elements) {
        this.graph = new GraphWithoutPayload<>(true);

        // Create a graph of all overlapping objects
        for (int i = 0; i < elements.size(); i++) {

            ObjectMask first = elements.get(i).getElement();

            graph.addVertex(first);

            for (int j = 0; j < i; j++) {
                ObjectMask second = elements.get(j).getElement();

                if (first.hasIntersectingVoxels(second)) {
                    graph.addEdge(first, second);
                }
            }
        }
    }

    public void mergeVerticesInGraph(
            ObjectMask sourceElement, ObjectMask overlappingElement, ObjectMask merged)
            throws OperationFailedException {
        Collection<ObjectMask> adjacentSource = graph.adjacentVerticesOutgoing(sourceElement);
        Collection<ObjectMask> adjacentOverlapping =
                graph.adjacentVerticesOutgoing(overlappingElement);

        graph.removeVertex(sourceElement);
        graph.removeVertex(overlappingElement);

        graph.addVertex(merged);

        graph.addEdges(merged, adjacentSource);
        graph.addEdges(merged, adjacentOverlapping);
    }

    public void removeVertex(ObjectMask vertex) throws OperationFailedException {
        graph.removeVertex(vertex);
    }

    public void removeEdge(ObjectMask from, ObjectMask to) {
        graph.removeEdge(from, to);
    }

    public Collection<ObjectMask> adjacentVerticesOutgoing(ObjectMask vertex) {
        return graph.adjacentVerticesOutgoing(vertex);
    }
}
