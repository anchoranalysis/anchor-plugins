package org.anchoranalysis.plugin.image.obj.merge;

/*
 * #%L
 * anchor-image-feature
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.anchoranalysis.core.arithmetic.FloatUtilities;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Comparator3i;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.obj.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.obj.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.obj.merge.priority.PrioritisedVertex;


/**
 * A graph that stores each object as a vertex, where edges represent a neighbourhood relation.
 * 
 * <p>It potentially allows merges between neighbouring vertices.</p>
 * 
 * <p>Each vertex has a payload (double) value associated with it, that is a function of the object-mask.</p>
 * 
 * @author Owen Feehan
 *
 */
public class MergeGraph {
	
	// START: Set from constructor
	private PayloadCalculator payloadCalculator;
	// END: Set from constructor
	
	private NeighbourGraph graph;
	private GraphLogger logger;
	private AssignPriority prioritizer;
	
	/**
	 * Constructor
	 * 
	 * @param payloadCalculator means to calculate a payload for any object
	 * @param beforeCondition
	 * @param imageRes image-resolution
	 * @param prioritizer means to assign priority to the merge of any two objects
	 * @param logPayload whether to include the payload in logging messages
	 */
	public MergeGraph(
		PayloadCalculator payloadCalculator,
		UpdatableBeforeCondition beforeCondition,
		ImageRes imageRes,
		AssignPriority prioritizer,
		LogErrorReporter logger,
		boolean logPayload
	) {
		super();
		this.payloadCalculator = payloadCalculator;
		this.prioritizer = prioritizer;
				
		graph = new NeighbourGraph(beforeCondition,	imageRes);
		this.logger = new GraphLogger(
			new DescribeGraph(graph, logPayload),
			logger
		);
	}
	
	
	public List<ObjVertex> addObjsToGraph( ObjMaskCollection objs ) throws OperationFailedException {
		
		List<ObjVertex> listAdded = new ArrayList<>();				
		
		for( int i=0; i<objs.size(); i++ ) {

			ObjVertex vertex = createVertex(
				objs.get(i)
			);
			graph.addVertex( vertex, listAdded, prioritizer, logger);
			listAdded.add( vertex );
		}
		
		return listAdded;
	}
	
	
	public ObjVertex merge( EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> bestImprovement, LogErrorReporter logErrorReporter ) throws OperationFailedException {
		
		Set<ObjVertex> setPossibleNghbs = graph.neighbourNodesFor( bestImprovement );
		graph.removeVertex( bestImprovement.getNode1() );
		graph.removeVertex( bestImprovement.getNode2() );
		
		ObjVertex omMerged = bestImprovement.getEdge().getOmWithFeature();
		graph.addVertex( omMerged, setPossibleNghbs, prioritizer, logger);
		
		logger.describeMerge(omMerged, bestImprovement);
		
		return omMerged;
	}
	
	public EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> findMaxPriority() {
		
		EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> max = null;
		
		Comparator3i<Point3i> comparator = new Comparator3i<>();
		
		for( EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> entry : graph.edgeSetUnique() ) {
			
			PrioritisedVertex edge = entry.getEdge();
			
			// We skip any edges that don't off an improvement
			if (!edge.isConsiderForMerge()) {
				continue;
			}
			
			if (max==null || edge.getPriority()>max.getEdge().getPriority()) {
				max = entry;
			} else if ( FloatUtilities.areEqual(edge.getPriority(),max.getEdge().getPriority())) {
				
				// If we have equal values, we impose an arbitrary ordering
				// so as to keep the output of the algorithm as deterministic as possible
				int cmp = comparator.compare(
					max.getEdge().getOmWithFeature().getObjMask().findAnyPntOnMask(),
					edge.getOmWithFeature().getObjMask().findAnyPntOnMask()
				);
				if (cmp>0) {
					max = entry;
				}

				// It should only be possible to ever have cmp==0 unless objects overlap. If we get
				//  this far, we resign ourselves to non-determinism
			}
		}
		
		return max;
	}

	public void logGraphDescription() {
		logger.logDescription();
	}

	public ObjMaskCollection verticesAsObjects() {
		return graph.verticesAsObjects();
	}
	
	private ObjVertex createVertex( ObjMask obj ) throws OperationFailedException {
		try {
			return new ObjVertex(
				obj,
				payloadCalculator.calc(obj)
			); 
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
}