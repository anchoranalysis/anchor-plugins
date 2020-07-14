package org.anchoranalysis.plugin.image.object.merge;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.object.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/** 
 * A graph of objects that neighbour each other, according to conditions
 * 
 **/
class NeighbourGraph {
	
	private UpdatableBeforeCondition beforeCondition;
	private Optional<ImageResolution> res;
	
	private GraphWithEdgeTypes<ObjectVertex, PrioritisedVertex> graph = new GraphWithEdgeTypes<>(true);
	
	/**
	 * Constructor
	 * 
	 * @param beforeCondition
	 * @param imageRes
	 * @param prioritizer
	 */
	public NeighbourGraph(UpdatableBeforeCondition beforeCondition, Optional<ImageResolution> imageRes) {
		super();
		this.beforeCondition = beforeCondition;
		this.res = imageRes;
	}
	
	/**
	 * Adds a vertex to the graph, adding appropriate edges where neighbourhood conditions
	 *   are fulfilled with any of the objects in possibleNghbs
	 * 
	 * @param om an object already in the graph
	 * @param possibleNghbs other vertices in the graph that are possibly neighbours
	 * @param logger
	 * @throws OperationFailedException
	 */
	public void addVertex(
		ObjectVertex om,
		Collection<ObjectVertex> possibleNghbs,
		AssignPriority prioritizer,
		GraphLogger logger
	) throws OperationFailedException {
		
		graph.addVertex(om);
		
		beforeCondition.updateSourceObject(om.getObject(), res);
				
		for( ObjectVertex possibleNghb : possibleNghbs ) {
			maybeAddEdge(om, possibleNghb, prioritizer, logger);
		}
	}
	
	/** Get a set of all neighbouring vertices of the vertices on a particular edge (not including the vertices associated with the edge) */
	public Set<ObjectVertex> neighbourNodesFor( EdgeTypeWithVertices<ObjectVertex,PrioritisedVertex> edge ) {
		Set<ObjectVertex> setOut = new HashSet<>();
		addNghbsToSet( edge.getNode1(), setOut );
		addNghbsToSet( edge.getNode2(), setOut );
		setOut.remove( edge.getNode1() );
		setOut.remove( edge.getNode2() );
		return setOut;
	}
	
	/** Creates an object-mask collection representing all the objects in the vertices */
	public ObjectCollection verticesAsObjects() {
		return ObjectCollectionFactory.mapFrom(
			graph.vertexSet(),
			ObjectVertex::getObject
		);
	}
	
	private void addNghbsToSet( ObjectVertex vertex, Set<ObjectVertex> setPossibleNghbs ) {
		
		// Remove the nodes associated with this edge
		for( EdgeTypeWithVertices<ObjectVertex,PrioritisedVertex> edge : graph.edgesOf(vertex) ) {
			if(!edge.getNode1().equals(vertex)) {
				setPossibleNghbs.add(edge.getNode1());
			}
			if(!edge.getNode2().equals(vertex)) {
				setPossibleNghbs.add(edge.getNode2());
			}
		}
	}
			
	// Returns true if an edge is added, false otherwise
	private boolean maybeAddEdge(
		ObjectVertex src,
		ObjectVertex dest,
		AssignPriority prioritizer,
		GraphLogger logger
	) throws OperationFailedException {
		
		if(!beforeCondition.accept(dest.getObject())) {
			return false;
		}
			
		PrioritisedVertex withPriority = prioritizer.assignPriority(src, dest, logger);
		
		graph.addEdge(src, dest, withPriority );
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
