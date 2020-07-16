/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/** A graph of objects that neighbor each other, according to conditions */
@RequiredArgsConstructor
class NeighborGraph {

    // START REQUIRED ARGUMENTS
    private final UpdatableBeforeCondition beforeCondition;
    private final Optional<ImageResolution> res;
    // END REQUIRED ARGUMENTS

    private GraphWithEdgeTypes<ObjectVertex, PrioritisedVertex> graph =
            new GraphWithEdgeTypes<>(true);

    /**
     * Adds a vertex to the graph, adding appropriate edges where neighborhood conditions are
     * fulfilled with any of the objects in possible neighbors
     *
     * @param om an object already in the graph
     * @param possibleNeighbors other vertices in the graph that are possibly neighbors
     * @param logger
     * @throws OperationFailedException
     */
    public void addVertex(
            ObjectVertex om,
            Collection<ObjectVertex> possibleNeighbors,
            AssignPriority prioritizer,
            GraphLogger logger)
            throws OperationFailedException {

        graph.addVertex(om);

        beforeCondition.updateSourceObject(om.getObject(), res);

        for (ObjectVertex possibleNeighbor : possibleNeighbors) {
            maybeAddEdge(om, possibleNeighbor, prioritizer, logger);
        }
    }

    /**
     * Get a set of all neighboring vertices of the vertices on a particular edge (not including the
     * vertices associated with the edge)
     */
    public Set<ObjectVertex> neighborNodesFor(
            EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> edge) {
        Set<ObjectVertex> setOut = new HashSet<>();
        addNeighborsToSet(edge.getNode1(), setOut);
        addNeighborsToSet(edge.getNode2(), setOut);
        setOut.remove(edge.getNode1());
        setOut.remove(edge.getNode2());
        return setOut;
    }

    /** Creates an object-mask collection representing all the objects in the vertices */
    public ObjectCollection verticesAsObjects() {
        return ObjectCollectionFactory.mapFrom(graph.vertexSet(), ObjectVertex::getObject);
    }

    private void addNeighborsToSet(ObjectVertex vertex, Set<ObjectVertex> setPossibleNeighbors) {

        // Remove the nodes associated with this edge
        for (EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex> edge : graph.edgesOf(vertex)) {
            if (!edge.getNode1().equals(vertex)) {
                setPossibleNeighbors.add(edge.getNode1());
            }
            if (!edge.getNode2().equals(vertex)) {
                setPossibleNeighbors.add(edge.getNode2());
            }
        }
    }

    // Returns true if an edge is added, false otherwise
    private boolean maybeAddEdge(
            ObjectVertex src, ObjectVertex dest, AssignPriority prioritizer, GraphLogger logger)
            throws OperationFailedException {

        if (!beforeCondition.accept(dest.getObject())) {
            return false;
        }

        PrioritisedVertex withPriority = prioritizer.assignPriority(src, dest, logger);

        graph.addEdge(src, dest, withPriority);
        return true;
    }

    public void removeVertex(ObjectVertex node) {
        graph.removeVertex(node);
    }

    int numVertices() {
        return graph.numVertices();
    }

    int numEdges() {
        return graph.numEdges();
    }

    public Collection<EdgeTypeWithVertices<ObjectVertex, PrioritisedVertex>> edgeSetUnique() {
        return graph.edgeSetUnique();
    }

    Collection<ObjectVertex> vertexSet() {
        return graph.vertexSet();
    }
}
