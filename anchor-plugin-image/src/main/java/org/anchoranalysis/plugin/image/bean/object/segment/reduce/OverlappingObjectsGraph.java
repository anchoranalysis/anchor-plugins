package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.Collection;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.GraphWithoutPayload;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * A graph of overlapping-objects.
 * 
 * @author Owen Feehan
 *
 */
class OverlappingObjectsGraph {

    private final GraphWithoutPayload<ObjectMask> graph;
    
    /**
     * Creates graph of overlapping objects from a list of elements.
     * 
     * <p>Each object becomes a vertex in the graph, and when an object overlaps with another
     * an edge connects the two vertices.
     * 
     * @param elements the elements
     */
    public OverlappingObjectsGraph(List<WithConfidence<ObjectMask>> elements) {
        this.graph = new GraphWithoutPayload<>(true);
        
        // Create a graph of all overlapping objects
        for( int i=0; i<elements.size(); i++) {
            
            ObjectMask first = elements.get(i).getElement();
            
            graph.addVertex(first);
            
            for( int j=0; j<i; j++) {
                ObjectMask second = elements.get(j).getElement();
                
                if (first.hasIntersectingVoxels(second)) {
                    graph.addEdge(first, second);
                }
            }
        }
    }
    
    public void mergeVerticesInGraph(ObjectMask sourceElement, ObjectMask overlappingElement, ObjectMask merged) throws OperationFailedException {
        Collection<ObjectMask> adjacentSource = graph.adjacentVerticesOutgoing(sourceElement);
        Collection<ObjectMask> adjacentOverlapping = graph.adjacentVerticesOutgoing(overlappingElement);
        
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
