package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.graph.GraphWithoutPayload;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;

/**
 * Combines a {@link PriorityQueue} (ordering by highest confidence) and a graph-structure
 * indicating which objects overlap with each other.
 *
 * <p>Any operation that changes one structure, will also update the other structure.
 *
 * @author Owen Feehan
 */
class ReduceObjectsGraph {

    /** The priority queue that always gives priority to the highest-confidence object. */
    private final PriorityQueue<LabelledWithConfidence<ObjectMask>> queue;

    /** The graph with objects as vertices, and with an edge between any objects that intersect. */
    private final GraphWithoutPayload<LabelledWithConfidence<ObjectMask>> graph;

    public ReduceObjectsGraph(List<LabelledWithConfidence<ObjectMask>> elements) {

        /** Tracks which objects overlap with other objects, updated as merges/deletions occur. */
        graph = new IntersectingObjects<>(elements, LabelledWithConfidence::getElement).asGraph();

        queue = new PriorityQueue<>(elements);
    }

    /**
     * Do no elements exist?
     *
     * @return true if no elemenets exist, false if at least one element exists.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * The element at the top of the queue, returned without modifying the queue.
     *
     * @return the highest-priority element
     */
    public LabelledWithConfidence<ObjectMask> peek() {
        return queue.peek();
    }

    /**
     * The element at the top of the queue, returned removing it from the queue and graph.
     *
     * @return the highest-priority element
     */
    public LabelledWithConfidence<ObjectMask> poll() {
        LabelledWithConfidence<ObjectMask> element = queue.poll();
        try {
            graph.removeVertex(element);
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        return element;
    }

    /**
     * The vertices that are connected to a particular vertex by an outgoing edge.
     *
     * @param vertex the vertex to find adjacent vertices for.
     * @return all vertices to which an outgoing edge exists from {@code vertex}.
     */
    public List<LabelledWithConfidence<ObjectMask>> adjacentVerticesOutgoing(
            LabelledWithConfidence<ObjectMask> vertex) {
        return graph.adjacentVerticesOutgoing(vertex);
    }

    /**
     * Like {@link #adjacentVerticesOutgoing} but returns a {@link Stream} instead of a {@link Set}.
     *
     * @param vertex the vertex to find adjacent vertices for.
     * @return all vertices to which an outgoing edge exists from {@code vertex}.
     */
    public Stream<LabelledWithConfidence<ObjectMask>> adjacentVerticesOutgoingStream(
            LabelledWithConfidence<ObjectMask> vertex) {
        return graph.adjacentVerticesOutgoingStream(vertex);
    }

    /**
     * Remove an edge between two vertices.
     *
     * <p>For an undirected graph, the directionality is irrelevant, and will achieve the same
     * effect, whatever the order of {@code from} and {@code to}.
     *
     * @param from the vertex the edge joins <i>from</i>.
     * @param to the vertex the edge joins <i>to</i>.
     */
    public void removeEdge(
            LabelledWithConfidence<ObjectMask> from, LabelledWithConfidence<ObjectMask> to) {
        graph.removeEdge(from, to);
    }

    /**
     * Removes a vertex and any edges connected to it.
     *
     * @param vertex the vertex to remove
     * @throws OperationFailedException if the vertex doesn't exist in the graph.
     */
    public void removeVertex(LabelledWithConfidence<ObjectMask> vertex)
            throws OperationFailedException {
        // This involves a linear search
        queue.remove(vertex);

        graph.removeVertex(vertex);
    }

    /**
     * Merges two existing vertices together.
     *
     * <p>The two existing vertices are replaced with {@code merged}.
     *
     * <p>Existing incoming and outgoing edges for the two vertices are then connected instead to
     * {@code merged}.
     *
     * @param element1 the first element to merge.
     * @param element2 the second element to merge.
     * @param merged the merged element that replaces {@code element1} and {@code element2}.
     * @throws OperationFailedException
     */
    public void mergeVertices(
            LabelledWithConfidence<ObjectMask> element1,
            LabelledWithConfidence<ObjectMask> element2,
            LabelledWithConfidence<ObjectMask> merged)
            throws OperationFailedException {
        graph.mergeVertices(element1, element2, merged);

        // This involves two linear searches
        queue.remove(element1);
        queue.remove(element2);
        queue.add(merged);
    }
}
