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

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.graph.GraphWithPayload;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/** A graph of objects that neighbor each other, according to conditions. */
@RequiredArgsConstructor
class NeighborGraph {

    /** Condition to check before adding edges between vertices. */
    private final UpdatableBeforeCondition beforeCondition;

    /** Optional converter for units from voxels to physical measurements and vice-versa. */
    private final Optional<UnitConverter> unitConverter;

    /** The underlying graph structure. */
    private GraphWithPayload<ObjectVertex, PrioritisedVertex> graph = new GraphWithPayload<>(true);

    /**
     * Adds a vertex to the graph, adding appropriate edges where neighborhood conditions are
     * fulfilled with any of the objects in possible neighbors.
     *
     * @param vertex an {@link ObjectVertex} to be added to the graph
     * @param possibleNeighbors other vertices in the graph that are possibly neighbors
     * @param prioritizer {@link AssignPriority} to assign priority to potential edges
     * @param logger {@link GraphLogger} for logging operations
     * @throws OperationFailedException if the operation fails
     */
    public void addVertex(
            ObjectVertex vertex,
            Collection<ObjectVertex> possibleNeighbors,
            AssignPriority prioritizer,
            GraphLogger logger)
            throws OperationFailedException {

        graph.addVertex(vertex);

        beforeCondition.updateSourceObject(vertex.getObject(), unitConverter);

        for (ObjectVertex possibleNeighbor : possibleNeighbors) {
            maybeAddEdge(vertex, possibleNeighbor, prioritizer, logger);
        }
    }

    /**
     * Get a set of all neighboring vertices of the vertices on a particular edge (not including the
     * vertices associated with the edge).
     *
     * @param edge the {@link TypedEdge} to find neighbors for
     * @return a {@link Set} of neighboring {@link ObjectVertex}
     */
    public Set<ObjectVertex> neighborNodesFor(TypedEdge<ObjectVertex, PrioritisedVertex> edge) {
        Set<ObjectVertex> setOut = new HashSet<>();
        addNeighborsToSet(edge.getFrom(), setOut);
        addNeighborsToSet(edge.getTo(), setOut);
        setOut.remove(edge.getFrom());
        setOut.remove(edge.getTo());
        return setOut;
    }

    /**
     * Creates an object-mask collection representing all the objects in the vertices.
     *
     * @return an {@link ObjectCollection} of all objects in the graph
     */
    public ObjectCollection verticesAsObjects() {
        return ObjectCollectionFactory.mapFrom(graph.vertices(), ObjectVertex::getObject);
    }

    /**
     * Removes a vertex from the graph.
     *
     * @param node the {@link ObjectVertex} to remove
     * @throws OperationFailedException if the removal fails
     */
    public void removeVertex(ObjectVertex node) throws OperationFailedException {
        graph.removeVertex(node);
    }

    /**
     * Gets the number of vertices in the graph.
     *
     * @return the number of vertices
     */
    int numberVertices() {
        return graph.numberVertices();
    }

    /**
     * Gets a collection of all unique edges in the graph.
     *
     * @return a {@link Collection} of {@link TypedEdge}
     */
    public Collection<TypedEdge<ObjectVertex, PrioritisedVertex>> edgeSetUnique() {
        return graph.edgesUnique();
    }

    /**
     * Gets a collection of all vertices in the graph.
     *
     * @return a {@link Collection} of {@link ObjectVertex}
     */
    Collection<ObjectVertex> vertexSet() {
        return graph.vertices();
    }

    /**
     * Adds neighboring vertices of a given vertex to a set.
     *
     * @param vertex the {@link ObjectVertex} to find neighbors for
     * @param setPossibleNeighbors the {@link Set} to add neighbors to
     */
    private void addNeighborsToSet(ObjectVertex vertex, Set<ObjectVertex> setPossibleNeighbors) {

        // Remove the nodes associated with this edge
        for (TypedEdge<ObjectVertex, PrioritisedVertex> edge : graph.outgoingEdgesFor(vertex)) {
            if (!edge.getFrom().equals(vertex)) {
                setPossibleNeighbors.add(edge.getFrom());
            }
            if (!edge.getTo().equals(vertex)) {
                setPossibleNeighbors.add(edge.getTo());
            }
        }
    }

    /**
     * Attempts to add an edge between two vertices if conditions are met.
     *
     * @param from the source {@link ObjectVertex}
     * @param to the destination {@link ObjectVertex}
     * @param prioritizer {@link AssignPriority} to assign priority to the potential edge
     * @param logger {@link GraphLogger} for logging operations
     * @return true if an edge is added, false otherwise
     * @throws OperationFailedException if the operation fails
     */
    private boolean maybeAddEdge(
            ObjectVertex from, ObjectVertex to, AssignPriority prioritizer, GraphLogger logger)
            throws OperationFailedException {

        if (!beforeCondition.accept(to.getObject())) {
            return false;
        }

        PrioritisedVertex withPriority = prioritizer.assignPriority(from, to, logger);

        graph.addEdge(from, to, withPriority);
        return true;
    }
}
