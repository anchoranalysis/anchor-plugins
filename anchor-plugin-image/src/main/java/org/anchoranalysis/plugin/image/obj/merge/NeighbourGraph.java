package org.anchoranalysis.plugin.image.obj.merge;

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
import java.util.Set;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.obj.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.obj.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.obj.merge.priority.PrioritisedVertex;

/** 
 * A graph of objects that neighbour each other, according to conditions
 * 
 **/
class NeighbourGraph {
	
	private UpdatableBeforeCondition beforeCondition;
	private ImageRes res;
	
	private GraphWithEdgeTypes<ObjVertex, PrioritisedVertex> graph = new GraphWithEdgeTypes<>(true);
	
	/**
	 * Constructor
	 * 
	 * @param beforeCondition
	 * @param imageRes
	 * @param prioritizer
	 */
	public NeighbourGraph(UpdatableBeforeCondition beforeCondition, ImageRes imageRes) {
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
	 * @param logErrorReporter
	 * @throws OperationFailedException
	 */
	public void addVertex(
		ObjVertex om,
		Collection<ObjVertex> possibleNghbs,
		AssignPriority prioritizer,
		GraphLogger logger
	) throws OperationFailedException {
		
		graph.addVertex(om);
		
		beforeCondition.updateSrcObj(om.getObjMask(), res);
				
		for( ObjVertex possibleNghb : possibleNghbs ) {
			maybeAddEdge(om, possibleNghb, prioritizer, logger);
		}
	}
	
	/** Get a set of all neighbouring vertices of the vertices on a particular edge (not including the vertices associated with the edge) */
	public Set<ObjVertex> neighbourNodesFor( EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> edge ) {
		Set<ObjVertex> setOut = new HashSet<ObjVertex>();
		addNghbsToSet( edge.getNode1(), setOut );
		addNghbsToSet( edge.getNode2(), setOut );
		setOut.remove( edge.getNode1() );
		setOut.remove( edge.getNode2() );
		return setOut;
	}
	

	/** Creates an object-mask collection representing all the objects in the vertices */
	public ObjMaskCollection verticesAsObjects() {
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( ObjVertex omWithFeature : graph.vertexSet() ) {
			out.add( omWithFeature.getObjMask() );
		}
		return out;
	}
	
	
	private void addNghbsToSet( ObjVertex vertex, Set<ObjVertex> setPossibleNghbs ) {
		
		// Remove the nodes associated with this edge
		for( EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> edge : graph.edgesOf(vertex) ) {
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
		ObjVertex src,
		ObjVertex dest,
		AssignPriority prioritizer,
		GraphLogger logger
	) throws OperationFailedException {
		
		if(!beforeCondition.accept(dest.getObjMask())) {
			return false;
		}
			
		PrioritisedVertex withPriority = prioritizer.assignPriority(src, dest, logger);
		
		graph.addEdge(src, dest, withPriority );
		return true;
	}

	public void removeVertex(ObjVertex node) {
		graph.removeVertex(node);
	}

	int numVertices() {
		return graph.numVertices();
	}

	int numEdges() {
		return graph.numEdges();
	}

	public Collection<EdgeTypeWithVertices<ObjVertex, PrioritisedVertex>> edgeSetUnique() {
		return graph.edgeSetUnique();
	}

	Collection<ObjVertex> vertexSet() {
		return graph.vertexSet();
	}
}
