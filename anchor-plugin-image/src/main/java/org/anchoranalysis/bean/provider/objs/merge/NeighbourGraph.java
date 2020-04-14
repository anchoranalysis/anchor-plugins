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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.anchoranalysis.bean.provider.objs.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;

/** A graph of objects that neighbour each other, according to conditions */
public class NeighbourGraph {
	
	private UpdatableBeforeCondition beforeCondition;
	private ImageRes res;

	private EdgeCreator edgeCreator;
	
	private GraphWithEdgeTypes<ObjMaskWithFeature, ObjMaskWithFeatureAndPriority> graph = new GraphWithEdgeTypes<>(true);
	
	public static interface EdgeCreator {
		ObjMaskWithFeatureAndPriority createPriority(ObjMaskWithFeature omSrcWithFeature, ObjMaskWithFeature omDestWithFeature, LogErrorReporter logger) throws OperationFailedException;
	}
	
	public NeighbourGraph(UpdatableBeforeCondition beforeCondition, ImageRes res, EdgeCreator edgeCreator) {
		super();
		this.beforeCondition = beforeCondition;
		this.res = res;
		this.edgeCreator = edgeCreator;
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
			ObjMaskWithFeature om,
			Collection<ObjMaskWithFeature> possibleNghbs,
			LogErrorReporter logErrorReporter
		) throws OperationFailedException {
		
		graph.addVertex(om);
		
		beforeCondition.updateSrcObj(om.getObjMask(), res);
				
		for( ObjMaskWithFeature possibleNghb : possibleNghbs ) {
			
			if (maybeAddEdge(om, possibleNghb, logErrorReporter)) {
				//System.out.printf("Add nghb to merged (%f and %f)\n",omMerged.getFeatureVal(),possibleNghb.getFeatureVal() );
			}
		}
	}
	
	// Get a set of all neighbouring vertices of the vertices on a particular edge (not including the vertices associated with the edge)
	public Set<ObjMaskWithFeature> neighbourNodesFor( EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> edge ) {
		Set<ObjMaskWithFeature> setOut = new HashSet<ObjMaskWithFeature>();
		addNghbsToSet( edge.getNode1(), setOut );
		addNghbsToSet( edge.getNode2(), setOut );
		setOut.remove( edge.getNode1() );
		setOut.remove( edge.getNode2() );
		return setOut;
	}
	

	public ObjMaskCollection createObjMaskCollectionFromVertices() {
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( ObjMaskWithFeature omWithFeature : graph.vertexSet() ) {
			out.add( omWithFeature.getObjMask() );
		}
		return out;
	}
	
	
	private void addNghbsToSet( ObjMaskWithFeature vertex, Set<ObjMaskWithFeature> setPossibleNghbs ) {
		
		// Remove the nodes associated with this edge
		for( EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> edge : graph.edgesOf(vertex) ) {
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
		ObjMaskWithFeature omSrcWithFeature,
		ObjMaskWithFeature omDestWithFeature,
		LogErrorReporter logger
	) throws OperationFailedException {
		
		if(!beforeCondition.accept(omDestWithFeature.getObjMask())) {
			return false;
		}
			
		ObjMaskWithFeatureAndPriority withPriority = edgeCreator.createPriority(
			omSrcWithFeature,
			omDestWithFeature,
			logger
		);
		
		graph.addEdge(omSrcWithFeature, omDestWithFeature, withPriority );
		return true;
	}

	public void removeVertex(ObjMaskWithFeature node) {
		graph.removeVertex(node);
	}

	int numVertices() {
		return graph.numVertices();
	}

	int numEdges() {
		return graph.numEdges();
	}

	public Collection<EdgeTypeWithVertices<ObjMaskWithFeature, ObjMaskWithFeatureAndPriority>> edgeSetUnique() {
		return graph.edgeSetUnique();
	}

	Collection<ObjMaskWithFeature> vertexSet() {
		return graph.vertexSet();
	}
}
